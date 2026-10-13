import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PlayerService } from '../../../core/services/player.service';
import { AuthService } from '../../../core/services/auth.service';
import { BookmarkService } from '../../../core/services/bookmark.service';
import { PlayerPosition, PlayerPositionLabels, PlayerProfile } from '../../../core/models/models';
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
  public players: PlayerProfile[] = [];
  public selectedPosition: PlayerPosition | '' = '';
  public searchQuery = '';
  public age: number | null = null;
  public isLoading = false;

  public get filteredPlayers(): PlayerProfile[] {
    if (!this.searchQuery || this.searchQuery.trim() === '') {
      return this.players;
    }
    const q = this.searchQuery.toLowerCase().trim();
    return this.players.filter(p => 
      p.fullName.toLowerCase().includes(q) || 
      (p.bio && p.bio.toLowerCase().includes(q))
    );
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
  public selectedRadarPlayer: PlayerProfile | null = null;

  constructor(
    private playerService: PlayerService,
    public bookmarkService: BookmarkService,
    public authService: AuthService
  ) {}

  public openTrialModal(player: PlayerProfile, event: Event): void {
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

  public openRadarModal(player: PlayerProfile, event: Event): void {
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

  public toggleBookmark(player: PlayerProfile, event: Event): void {
    event.stopPropagation();
    this.bookmarkService.toggleBookmark(player.id).subscribe();
  }

  public isBookmarked(playerId: number): boolean {
    return this.bookmarkService.isBookmarked(playerId);
  }

  public loadPlayers(): void {
    this.isLoading = true;
    this.playerService.getPlayers(
      this.selectedPosition,
      this.age
    ).subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res.success && res.data) {
          this.players = res.data;
        }
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  public onFilterChange(): void {
    this.loadPlayers();
  }

  public resetFilters(): void {
    this.selectedPosition = '';
    this.searchQuery = '';
    this.age = null;
    this.loadPlayers();
  }

  public openVideo(player: PlayerProfile): void {
    this.selectedVideoUrl = player.videoUrl || '';
    this.selectedVideoUrls = player.videoUrls && player.videoUrls.length > 0 ? player.videoUrls : (player.videoUrl ? [player.videoUrl] : []);
    this.selectedPlayerName = player.fullName;
    this.isVideoModalOpen = true;
  }

  public closeVideoModal(): void {
    this.isVideoModalOpen = false;
  }
}
