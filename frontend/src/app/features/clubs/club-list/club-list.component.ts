import { Component, OnInit, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Meta, Title } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { ClubService } from '../../../core/services/club.service';
import { PlayerService } from '../../../core/services/player.service';
import { ClubProfile } from '../../../core/models/models';

@Component({
  selector: 'app-club-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './club-list.component.html',
  styleUrls: ['./club-list.component.scss']
})
export class ClubListComponent implements OnInit {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly title = inject(Title);
  private readonly meta = inject(Meta);
  public clubs: ClubProfile[] = [];
  public isLoading = false;

  constructor(private clubService: ClubService, private playerService: PlayerService) {}

  public mediaUrl(value?: string | null): string { return this.playerService.imageUrl(value); }

  ngOnInit(): void {
    this.title.setTitle('الأندية الرياضية المسجلة | الكورة لعبتنا');
    this.meta.updateTag({ name: 'description', content: 'تصفح ملفات الأندية الرياضية المسجلة وفرص استكشاف اللاعبين.' });
    if (!isPlatformBrowser(this.platformId)) return;
    this.loadClubs();
  }

  public loadClubs(): void {
    this.isLoading = true;
    this.clubService.getClubs().subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res.success && res.data) {
          this.clubs = res.data;
        }
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }
}
