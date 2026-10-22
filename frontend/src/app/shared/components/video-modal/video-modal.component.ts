import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
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

  constructor(private sanitizer: DomSanitizer, private http: HttpClient) {}

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
    const item = this.videoItems[index];
    if (item) {
      this.currentRawUrl = item.rawUrl;
      this.currentThumbnail = item.thumbnailUrl;
      if (item.type === 'hosted') {
        const key = item.rawUrl.substring('platform-video:'.length);
        this.safeEmbedUrl = null;
        this.isDirectVideo = false;
        this.http.get<{ success: boolean; data: string }>(`/api/v1/players/videos/${key}`).subscribe({
          next: (res) => { if (this.selectedIndex === index && res.success) this.currentRawUrl = res.data; },
          error: () => { this.currentRawUrl = ''; }
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
    for (const u of rawList) {
      if (!u?.trim()) continue;
      const trimmed = u.trim();
      items.push(this.parseVideoItem(trimmed, counter++));
    }

    this.videoItems = items;
  }

  private parseVideoItem(url: string, idx: number): VideoItem {
    if (url.startsWith('platform-video:')) {
      return { rawUrl: url, youtubeId: null, thumbnailUrl: null, label: `مقطع ${idx}`, type: 'hosted' };
    }
    if (url.startsWith('platform-video:')) {
      return { rawUrl: url, youtubeId: null, thumbnailUrl: null, label: `مقطع ${idx}`, type: 'hosted' };
    }
    // ── YouTube ──
    const ytMatch = url.match(/(?:youtu\.be\/|youtube\.com\/(?:embed\/|v\/|watch\?v=|watch\?.+&v=|shorts\/|live\/))([\w-]{11})/i);
    if (ytMatch?.[1]) {
      const id = ytMatch[1];
      return {
        rawUrl: url,
        youtubeId: id,
        thumbnailUrl: `https://img.youtube.com/vi/${id}/hqdefault.jpg`,
        label: `مقطع ${idx}`,
        type: 'youtube'
      };
    }

    // ── Google Drive ──
    const driveMatch = url.match(/drive\.google\.com\/file\/d\/([\w-]+)/i);
    if (driveMatch?.[1]) {
      return {
        rawUrl: url,
        youtubeId: null,
        thumbnailUrl: null,
        label: `مقطع ${idx}`,
        type: 'drive'
      };
    }

    // ── Vimeo ──
    const vimeoMatch = url.match(/vimeo\.com\/(?:video\/)?([0-9]+)/i);
    if (vimeoMatch?.[1]) {
      return {
        rawUrl: url,
        youtubeId: null,
        thumbnailUrl: null,
        label: `مقطع ${idx}`,
        type: 'vimeo'
      };
    }

    // ── Direct MP4 etc ──
    const lower = url.toLowerCase();
    if (/\.(mp4|webm|ogg|mov|m4v)(\?.*)?$/.test(lower) || lower.startsWith('data:video/')) {
      return {
        rawUrl: url,
        youtubeId: null,
        thumbnailUrl: null,
        label: `مقطع ${idx}`,
        type: 'direct'
      };
    }

    return {
      rawUrl: url,
      youtubeId: null,
      thumbnailUrl: null,
      label: `مقطع ${idx}`,
      type: 'unknown'
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
        const id = item.rawUrl.match(/drive\.google\.com\/file\/d\/([\w-]+)/i)?.[1];
        const embedUrl = `https://drive.google.com/file/d/${id}/preview?usp=sharing`;
        this.safeEmbedUrl = this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl);
        break;
      }
      case 'vimeo': {
        const id = item.rawUrl.match(/vimeo\.com\/(?:video\/)?([0-9]+)/i)?.[1];
        const embedUrl = `https://player.vimeo.com/video/${id}?autoplay=1&title=0&byline=0&portrait=0&color=C9974A`;
        this.safeEmbedUrl = this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl);
        break;
      }
      case 'direct': {
        this.isDirectVideo = true;
        break;
      }
      default: {
        if (item.rawUrl.startsWith('http')) {
          this.safeEmbedUrl = this.sanitizer.bypassSecurityTrustResourceUrl(item.rawUrl);
        }
      }
    }
  }
}
