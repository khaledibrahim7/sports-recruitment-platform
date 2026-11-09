import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges, HostListener, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';

export interface VideoItem {
  rawUrl: string;
  youtubeId: string | null;
  thumbnailUrl: string | null;
  label: string;
  type: 'youtube' | 'drive' | 'vimeo' | 'direct' | 'hosted' | 'unknown';
}

export function parseSupportedVideoUrl(input: string): Pick<VideoItem, 'youtubeId' | 'thumbnailUrl' | 'type'> | null {
  if (input.startsWith('platform-video:') && input.length > 'platform-video:'.length) {
    return { youtubeId: null, thumbnailUrl: null, type: 'hosted' };
  }
  let url: URL;
  try { url = new URL(input); } catch { return null; }
  if (url.protocol !== 'https:') return null;
  const host = url.hostname.toLowerCase().replace(/^www\./, '');

  if (['youtube.com', 'm.youtube.com', 'youtube-nocookie.com', 'youtu.be'].includes(host)) {
    let id: string | null = null;
    if (host === 'youtu.be') id = url.pathname.split('/').filter(Boolean)[0] ?? null;
    else if (url.pathname === '/watch') id = url.searchParams.get('v');
    else if (/^\/(embed|v|shorts|live)\//.test(url.pathname)) id = url.pathname.split('/')[2] ?? null;
    if (!id || !/^[\w-]{11}$/.test(id)) return null;
    return { youtubeId: id, thumbnailUrl: `https://img.youtube.com/vi/${id}/hqdefault.jpg`, type: 'youtube' };
  }
  if (host === 'drive.google.com') {
    const id = url.pathname.match(/^\/file\/d\/([\w-]+)(?:\/|$)/)?.[1];
    return id ? { youtubeId: null, thumbnailUrl: null, type: 'drive' } : null;
  }
  if (host === 'vimeo.com' || host === 'player.vimeo.com') {
    const id = url.pathname.match(/^\/(?:video\/)?(\d+)(?:\/|$)/)?.[1];
    return id ? { youtubeId: null, thumbnailUrl: null, type: 'vimeo' } : null;
  }
  if (/\.(mp4|webm|ogg|mov|m4v)$/i.test(url.pathname)) {
    return { youtubeId: null, thumbnailUrl: null, type: 'direct' };
  }
  return null;
}

@Component({
  selector: 'app-video-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './video-modal.component.html',
  styleUrls: ['./video-modal.component.scss']
})
export class VideoModalComponent implements OnChanges {
  @Input() isOpen = false;
  @Input() videoUrl: string = '';
  @Input() videoUrls: string[] = [];
  @Input() playerName: string = '';
  @Output() close = new EventEmitter<void>();

  public safeEmbedUrl: SafeResourceUrl | null = null;
  public isDirectVideo: boolean = false;
  public selectedIndex = 0;
  public currentRawUrl: string = '';
  public videoItems: VideoItem[] = [];
  public get activeVideoList(): string[] { return this.videoItems.map(item => item.rawUrl); }

  // Poster (thumbnail) shown before play
  public showPoster = true;
  public currentThumbnail: string | null = null;

  private readonly sanitizer = inject(DomSanitizer);
  private readonly http = inject(HttpClient);

  public onPosterThumbError(event: Event): void {
    const img = event.target as HTMLImageElement | null;
    if (img) img.src = '';
  }

  public onPlaylistThumbError(event: Event): void {
    const img = event.target as HTMLImageElement | null;
    img?.parentElement?.classList.add('no-thumb');
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isOpen'] && this.isOpen) {
      this.showPoster = true;
    }
    this.buildVideoItems();
    if (this.videoItems.length > 0) {
      this.selectVideo(0);
    } else {
      this.currentRawUrl = '';
      this.safeEmbedUrl = null;
      this.isDirectVideo = false;
      this.currentThumbnail = null;
    }
  }

  public selectVideo(index: number): void {
    this.selectedIndex = index;
    this.showPoster = true;
    this.isDirectVideo = false;
    this.safeEmbedUrl = null;
    const item = this.videoItems[index];
    if (item) {
      this.currentRawUrl = item.rawUrl;
      this.currentThumbnail = item.thumbnailUrl;
      if (item.type === 'hosted') {
        const key = item.rawUrl.substring('platform-video:'.length);
        this.safeEmbedUrl = null;
        this.isDirectVideo = false;
        this.http.get<{ success: boolean; data: string }>('/api/v1/players/videos', { params: { key } }).subscribe({
          next: (res) => {
            if (this.selectedIndex === index && res.success) {
              this.currentRawUrl = res.data;
              this.isDirectVideo = true;
            }
          },
          error: () => { if (this.selectedIndex === index) this.currentRawUrl = ''; }
        });
      } else {
        this.processVideoItem(item);
      }
    }
  }

  public playVideo(): void {
    this.showPoster = false;
  }

  public closeModal(): void {
    this.showPoster = true;
    this.close.emit();
  }

  @HostListener('document:keydown.escape')
  public handleEscape(): void {
    if (this.isOpen) this.closeModal();
  }

  // ── Build de-duplicated list with metadata ──
  private buildVideoItems(): void {
    const items: VideoItem[] = [];

    const rawList: string[] = [];

    if (this.videoUrls && this.videoUrls.length > 0) {
      rawList.push(...this.videoUrls);
    }
    if (rawList.length === 0 && this.videoUrl?.trim()) {
      rawList.push(this.videoUrl.trim());
    }

    let counter = 1;
    const seen = new Set<string>();
    for (const u of rawList) {
      if (!u?.trim()) continue;
      const trimmed = u.trim();
      const dedupeKey = trimmed;
      if (seen.has(dedupeKey)) continue;
      seen.add(dedupeKey);
      items.push(this.parseVideoItem(trimmed, counter++));
    }

    this.videoItems = items;
  }

  private parseVideoItem(url: string, idx: number): VideoItem {
    const parsed = parseSupportedVideoUrl(url);
    return {
      rawUrl: url,
      youtubeId: parsed?.youtubeId ?? null,
      thumbnailUrl: parsed?.thumbnailUrl ?? null,
      label: `مقطع ${idx}`,
      type: parsed?.type ?? 'unknown'
    };
  }

  private processVideoItem(item: VideoItem): void {
    this.isDirectVideo = false;
    this.safeEmbedUrl = null;

    switch (item.type) {
      case 'youtube': {
        // Use youtube-nocookie.com for privacy + hide all branding
        const embedUrl = `https://www.youtube-nocookie.com/embed/${item.youtubeId}`
          + `?autoplay=1&rel=0&controls=1&showinfo=0`
          + `&iv_load_policy=3&disablekb=0&fs=1&playsinline=1&color=white`
          + `&enablejsapi=0&origin=${encodeURIComponent(window.location.origin)}`;
        this.safeEmbedUrl = this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl);
        break;
      }
      case 'drive': {
        const id = new URL(item.rawUrl).pathname.match(/^\/file\/d\/([\w-]+)/)?.[1];
        if (!id) break;
        const embedUrl = `https://drive.google.com/file/d/${id}/preview?usp=sharing`;
        this.safeEmbedUrl = this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl);
        break;
      }
      case 'vimeo': {
        const id = new URL(item.rawUrl).pathname.match(/^\/(?:video\/)?(\d+)/)?.[1];
        if (!id) break;
        const embedUrl = `https://player.vimeo.com/video/${id}?autoplay=1&title=0&byline=0&portrait=0&color=C9974A`;
        this.safeEmbedUrl = this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl);
        break;
      }
      case 'direct': {
        this.isDirectVideo = true;
        break;
      }
      default: {
        this.safeEmbedUrl = null;
      }
    }
  }
}
