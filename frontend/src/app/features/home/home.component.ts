import { Component, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { PlayerService } from '../../core/services/player.service';
import { AcademyService } from '../../core/services/academy.service';
import { ClubService } from '../../core/services/club.service';
import { PlayerPositionLabels, PublicPlayerProfile, AcademyProfile, ClubProfile } from '../../core/models/models';
import { VideoModalComponent } from '../../shared/components/video-modal/video-modal.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, VideoModalComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  public isLoggedIn = computed(() => this.authService.isLoggedIn());
  public currentUser = computed(() => this.authService.currentUserSignal());

  public userRole = computed(() => this.currentUser()?.role);
  public userDisplayName = computed(() => {
    const user = this.currentUser();
    if (!user) return 'المستخدم';
    return user.email ? user.email.split('@')[0] : 'المستخدم';
  });

  public featuredPlayers: PublicPlayerProfile[] = [];
  public registeredPlayerCount = 0;
  public featuredAcademies: AcademyProfile[] = [];
  public featuredClubs: ClubProfile[] = [];
  public playerPositionLabels = PlayerPositionLabels;

  public get spotlightPlayer(): PublicPlayerProfile | null {
    return this.featuredPlayers && this.featuredPlayers.length > 0 ? this.featuredPlayers[0] : null;
  }

  // Video Modal State
  public isVideoModalOpen = false;
  public selectedVideoUrl = '';
  public selectedVideoUrls: string[] = [];
  public selectedPlayerName = '';

  constructor(
    public authService: AuthService,
    private playerService: PlayerService,
    private academyService: AcademyService,
    private clubService: ClubService
  ) {}

  ngOnInit(): void {
    this.loadFeaturedData();
  }

  public openVideo(player: PublicPlayerProfile): void {
    // Pass full videoUrls array - fallback to single videoUrl if no array
    if (player.videoUrls && player.videoUrls.length > 0) {
      this.selectedVideoUrls = player.videoUrls;
      this.selectedVideoUrl = '';
    } else {
      this.selectedVideoUrls = [];
      this.selectedVideoUrl = player.videoUrl || '';
    }
    this.selectedPlayerName = player.fullName;
    this.isVideoModalOpen = true;
  }

  public closeVideoModal(): void {
    this.isVideoModalOpen = false;
  }

  private loadFeaturedData(): void {
    this.playerService.getPlayers('', null, '', 0, 6).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.featuredPlayers = res.data.items;
          this.registeredPlayerCount = res.data.totalElements;
        }
      },
      error: () => {}
    });

    this.academyService.getAcademies().subscribe({
      next: (res) => { if (res.success && res.data) this.featuredAcademies = res.data; },
      error: () => {}
    });

    this.clubService.getClubs().subscribe({
      next: (res) => { if (res.success && res.data) this.featuredClubs = res.data; },
      error: () => {}
    });
  }
}
