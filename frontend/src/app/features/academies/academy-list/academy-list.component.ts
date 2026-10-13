import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AcademyService } from '../../../core/services/academy.service';
import { AcademyProfile } from '../../../core/models/models';

@Component({
  selector: 'app-academy-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './academy-list.component.html',
  styleUrls: ['./academy-list.component.scss']
})
export class AcademyListComponent implements OnInit {
  public academies: AcademyProfile[] = [];
  public isLoading = false;

  constructor(private academyService: AcademyService) {}

  ngOnInit(): void {
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
