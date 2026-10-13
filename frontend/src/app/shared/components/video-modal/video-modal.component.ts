import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

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
  public activeVideoList: string[] = [];

  constructor(private sanitizer: DomSanitizer) {}

  ngOnChanges(changes: SimpleChanges): void {
    this.buildActiveList();
    if (this.activeVideoList.length > 0) {
      this.selectVideo(0);
    } else {
      this.currentRawUrl = '';
      this.safeEmbedUrl = null;
      this.isDirectVideo = false;
    }
  }

  public selectVideo(index: number): void {
    this.selectedIndex = index;
    if (this.activeVideoList && this.activeVideoList[index]) {
      this.processVideoUrl(this.activeVideoList[index]);
    }
  }

  public closeModal(): void {
    this.close.emit();
  }

  private buildActiveList(): void {
    const list: string[] = [];
    if (this.videoUrls && this.videoUrls.length > 0) {
      for (const u of this.videoUrls) {
        if (u && u.trim().length > 0 && !list.includes(u.trim())) {
          list.push(u.trim());
        }
      }
    }
    if (list.length === 0 && this.videoUrl && this.videoUrl.trim().length > 0) {
      list.push(this.videoUrl.trim());
    }
    this.activeVideoList = list;
  }

  private processVideoUrl(url: string): void {
    if (!url) return;
    this.currentRawUrl = url.trim();

    // 1. YouTube check (includes watch?v=, Shorts, embed/, live/, youtu.be/)
    const ytMatch = this.currentRawUrl.match(/(?:youtu\.be\/|youtube\.com\/(?:embed\/|v\/|watch\?v=|watch\?.+&v=|shorts\/|live\/))([\w-]{11})/i);
    if (ytMatch && ytMatch[1]) {
      const embedUrl = `https://www.youtube.com/embed/${ytMatch[1]}?autoplay=1&rel=0&modestbranding=1&controls=1&showinfo=0&iv_load_policy=3&disablekb=0`;
      this.safeEmbedUrl = this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl);
      this.isDirectVideo = false;
      return;
    }

    // 2. Google Drive check: drive.google.com/file/d/{FILE_ID}/...
    const driveMatch = this.currentRawUrl.match(/drive\.google\.com\/file\/d\/([\w-]+)/i);
    if (driveMatch && driveMatch[1]) {
      const embedUrl = `https://drive.google.com/file/d/${driveMatch[1]}/preview`;
      this.safeEmbedUrl = this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl);
      this.isDirectVideo = false;
      return;
    }

    // 3. Vimeo check
    const vimeoMatch = this.currentRawUrl.match(/vimeo\.com\/(?:video\/)?([0-9]+)/i);
    if (vimeoMatch && vimeoMatch[1]) {
      const embedUrl = `https://player.vimeo.com/video/${vimeoMatch[1]}?autoplay=1`;
      this.safeEmbedUrl = this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl);
      this.isDirectVideo = false;
      return;
    }

    // 4. Direct MP4 / WebM / OGG / MOV video check
    const lowerUrl = this.currentRawUrl.toLowerCase();
    if (
      lowerUrl.endsWith('.mp4') ||
      lowerUrl.endsWith('.webm') ||
      lowerUrl.endsWith('.ogg') ||
      lowerUrl.endsWith('.mov') ||
      lowerUrl.endsWith('.m4v') ||
      lowerUrl.includes('.mp4?') ||
      lowerUrl.startsWith('data:video/')
    ) {
      this.isDirectVideo = true;
      this.safeEmbedUrl = null;
      return;
    }

    // 5. Fallback: Embed as sanitized resource URL if valid URL format
    this.isDirectVideo = false;
    if (this.currentRawUrl.startsWith('http://') || this.currentRawUrl.startsWith('https://')) {
      this.safeEmbedUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.currentRawUrl);
    } else {
      this.safeEmbedUrl = null;
    }
  }
}
