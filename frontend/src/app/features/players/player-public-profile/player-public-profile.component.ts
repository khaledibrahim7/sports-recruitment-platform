import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, PLATFORM_ID, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Meta, Title } from '@angular/platform-browser';
import { PlayerService } from '../../../core/services/player.service';
import {
  CompetitionLevelLabels,
  PlayerPositionLabels,
  PreferredFootLabels,
  PublicPlayerProfile,
  handleImageFallback
} from '../../../core/models/models';
import { VideoModalComponent } from '../../../shared/components/video-modal/video-modal.component';
import { RadarChartComponent } from '../../../shared/components/radar-chart/radar-chart.component';

@Component({
  selector: 'app-player-public-profile',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterLink, VideoModalComponent, RadarChartComponent],
  templateUrl: './player-public-profile.component.html',
  styleUrl: './player-public-profile.component.scss'
})
export class PlayerPublicProfileComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly title = inject(Title);
  private readonly meta = inject(Meta);
  private readonly players = inject(PlayerService);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly positions = PlayerPositionLabels;
  readonly preferredFootLabels = PreferredFootLabels;
  readonly competitionLevelLabels = CompetitionLevelLabels;
  readonly onAvatarError = handleImageFallback;

  player: PublicPlayerProfile | null = null;
  isLoading = true;
  loadError = '';
  isVideoOpen = false;

  ngOnInit(): void {
    this.loadPlayerProfile();
  }

  public loadPlayerProfile(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isSafeInteger(id) || id <= 0) {
      this.isLoading = false;
      this.loadError = 'رابط ملف اللاعب غير صالح.';
      this.cdr.markForCheck();
      return;
    }
    this.isLoading = true;
    this.loadError = '';
    this.players.getPlayerById(id).subscribe({
      next: response => {
        this.isLoading = false;
        if (!response.success || !response.data) {
          this.loadError = 'لم يتم العثور على ملف اللاعب.';
          this.cdr.markForCheck();
          return;
        }
        this.player = response.data;
        const description = this.player.bio?.trim().slice(0, 155)
          || `تعرّف على الملف الفني للاعب ${this.player.fullName} ومركزه ومؤشراته الرياضية.`;
        const pageTitle = `${this.player.fullName} | ملف لاعب | الكورة لعبتنا`;
        this.title.setTitle(pageTitle);
        this.meta.updateTag({ name: 'description', content: description });
        this.meta.updateTag({ property: 'og:type', content: 'profile' });
        this.meta.updateTag({ property: 'og:title', content: pageTitle });
        this.meta.updateTag({ property: 'og:description', content: description });
        this.meta.updateTag({ property: 'og:image', content: this.players.imageUrl(this.player.avatarUrl) });
        this.meta.updateTag({ name: 'twitter:card', content: 'summary' });
        this.cdr.markForCheck();
      },
      error: () => {
        this.isLoading = false;
        this.loadError = 'تعذر تحميل ملف اللاعب. حاول مرة أخرى.';
        this.cdr.markForCheck();
      }
    });
  }

  imageUrl(value?: string | null): string { return this.players.imageUrl(value); }

  openVideo(): void {
    if (isPlatformBrowser(this.platformId)) this.isVideoOpen = true;
  }
}
