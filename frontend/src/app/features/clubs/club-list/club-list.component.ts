import { Component, OnInit, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Meta, Title } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { ClubService } from '../../../core/services/club.service';
import { PlayerService } from '../../../core/services/player.service';
import { ToastService } from '../../../core/services/toast.service';
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
  private readonly toastService = inject(ToastService);
  public clubs: ClubProfile[] = [];
  public isLoading = false;
  public loadError = '';

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
    this.loadError = '';
    this.clubService.getClubs().subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res.success && res.data) {
          this.clubs = res.data;
        } else {
          this.clubs = [];
          this.loadError = 'تعذر تحميل قائمة الأندية. حاول مرة أخرى.';
          this.toastService.error(this.loadError);
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.clubs = [];
        this.loadError = err?.status === 0
          ? 'تعذر الاتصال بالخادم. تحقق من اتصال الإنترنت ثم أعد المحاولة.'
          : 'حدث خطأ أثناء تحميل قائمة الأندية. حاول مرة أخرى.';
        this.toastService.error(this.loadError);
      }
    });
  }
}
