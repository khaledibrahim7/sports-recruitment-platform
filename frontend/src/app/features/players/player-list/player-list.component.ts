import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PlayerService } from '../../../core/services/player.service';
import { AuthService } from '../../../core/services/auth.service';
import { BookmarkService } from '../../../core/services/bookmark.service';
import { PlayerPosition, PlayerPositionLabels, PublicPlayerProfile } from '../../../core/models/models';
import { VideoModalComponent } from '../../../shared/components/video-modal/video-modal.component';
import { RadarChartComponent } from '../../../shared/components/radar-chart/radar-chart.component';
import { TrialOfferModalComponent } from '../../../shared/components/trial-offer-modal/trial-offer-modal.component';

@Component({
  selector: 'app-player-list',
  standalone: true,
  imports: [CommonModule, FormsModule, VideoModalComponent, RadarChartComponent, TrialOfferModalComponent],
  templateUrl: './player-list.component.html',
  styleUrls: ['./player-list.component.scss']
})
export class PlayerListComponent implements OnInit {
  public players: PublicPlayerProfile[] = [];
  public selectedPosition: PlayerPosition | '' = '';
  public searchQuery = '';
  public age: number | null = null;
  public isLoading = false;
  public loadError = '';
  public currentPage = 0;
  public pageSize = 12;
  public totalPages = 0;
  public totalPlayers = 0;

  public get filteredPlayers(): PublicPlayerProfile[] {
    return this.players;
  }

  public playerPositions = Object.keys(PlayerPositionLabels) as PlayerPosition[];
  public playerPositionLabels = PlayerPositionLabels;

  // Video Modal
  public isVideoModalOpen = false;
  public selectedVideoUrl = '';
  public selectedVideoUrls: string[] = [];
  public selectedPlayerName = '';

  // Trial Offer Modal
  public isTrialModalOpen = false;
  public selectedTrialPlayerId: number | null = null;
  public selectedTrialPlayerName = '';

  // Radar Chart Modal
  public isRadarModalOpen = false;
  public selectedRadarPlayer: PublicPlayerProfile | null = null;

  constructor(
    private playerService: PlayerService,
    public bookmarkService: BookmarkService,
    public authService: AuthService
  ) {}

  public openTrialModal(player: PublicPlayerProfile, event: Event): void {
    event.stopPropagation();
    this.selectedTrialPlayerId = player.id;
    this.selectedTrialPlayerName = player.fullName;
    this.isTrialModalOpen = true;
  }

  public closeTrialModal(): void {
    this.isTrialModalOpen = false;
    this.selectedTrialPlayerId = null;
    this.selectedTrialPlayerName = '';
  }

  public openRadarModal(player: PublicPlayerProfile, event: Event): void {
    event.stopPropagation();
    this.selectedRadarPlayer = player;
    this.isRadarModalOpen = true;
  }

  public closeRadarModal(): void {
    this.isRadarModalOpen = false;
    this.selectedRadarPlayer = null;
  }

  ngOnInit(): void {
    this.loadPlayers();
    if (this.canBookmark()) {
      this.bookmarkService.loadBookmarkedIds();
    }
  }

  public canBookmark(): boolean {
    const role = this.authService.userRole();
    return role === 'ACADEMY' || role === 'CLUB';
  }

  public toggleBookmark(player: PublicPlayerProfile, event: Event): void {
    event.stopPropagation();
    this.bookmarkService.toggleBookmark(player.id).subscribe();
  }

  public isBookmarked(playerId: number): boolean {
    return this.bookmarkService.isBookmarked(playerId);
  }

  public loadPlayers(): void {
    this.isLoading = true;
    this.loadError = '';
    this.playerService.getPlayers(
      this.selectedPosition,
      this.age,
      this.searchQuery,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res.success && res.data) {
          this.players = res.data.items;
          this.currentPage = res.data.page;
          this.totalPages = res.data.totalPages;
          this.totalPlayers = res.data.totalElements;
        } else {
          this.players = [];
          this.loadError = 'تعذر تحميل دليل اللاعبين. حاول مرة أخرى.';
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.players = [];
        this.loadError = err.status === 0
          ? 'تعذر الاتصال بالخادم. تحقق من اتصال الإنترنت ثم أعد المحاولة.'
          : 'حدث خطأ أثناء تحميل دليل اللاعبين. حاول مرة أخرى.';
      }
    });
  }

  public onFilterChange(): void {
    this.currentPage = 0;
    this.loadPlayers();
  }

  public changePage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.currentPage) return;
    this.currentPage = page;
    this.loadPlayers();
    const container = document.querySelector('.page-header');
    container?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  public resetFilters(): void {
    this.selectedPosition = '';
    this.searchQuery = '';
    this.age = null;
    this.currentPage = 0;
    this.loadPlayers();
  }

  public openVideo(player: PublicPlayerProfile): void {
    this.selectedVideoUrl = player.videoUrl || '';
    this.selectedVideoUrls = player.videoUrls && player.videoUrls.length > 0 ? player.videoUrls : (player.videoUrl ? [player.videoUrl] : []);
    this.selectedPlayerName = player.fullName;
    this.isVideoModalOpen = true;
  }

  public closeVideoModal(): void {
    this.isVideoModalOpen = false;
  }
}
