import { Component, OnInit, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Meta, Title } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { AcademyService } from '../../../core/services/academy.service';
import { PlayerService } from '../../../core/services/player.service';
import { AcademyProfile } from '../../../core/models/models';

@Component({
  selector: 'app-academy-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './academy-list.component.html',
  styleUrls: ['./academy-list.component.scss']
})
export class AcademyListComponent implements OnInit {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly title = inject(Title);
  private readonly meta = inject(Meta);
  public academies: AcademyProfile[] = [];
  public isLoading = false;

  constructor(private academyService: AcademyService, private playerService: PlayerService) {}

  public mediaUrl(value?: string | null): string { return this.playerService.imageUrl(value); }

  ngOnInit(): void {
    this.title.setTitle('الأكاديميات الرياضية المسجلة | الكورة لعبتنا');
    this.meta.updateTag({ name: 'description', content: 'تصفح ملفات الأكاديميات الرياضية وبرامجها التدريبية المسجلة على المنصة.' });
    if (!isPlatformBrowser(this.platformId)) return;
    this.loadAcademies();
  }

  public loadAcademies(): void {
    this.isLoading = true;
    this.academyService.getAcademies().subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res.success && res.data) {
          this.academies = res.data;
        }
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }
}
