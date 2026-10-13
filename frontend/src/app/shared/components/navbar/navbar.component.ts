import { Component, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { VisitorService } from '../../../core/services/visitor.service';

import { NotificationsDropdownComponent } from '../notifications-dropdown/notifications-dropdown.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, NotificationsDropdownComponent],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  public isLoggedIn = computed(() => this.authService.isLoggedIn());
  public currentUser = computed(() => this.authService.currentUserSignal());
  public visitorCount = computed(() => this.visitorService.visitorCountSignal());

  constructor(
    public authService: AuthService,
    private visitorService: VisitorService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.visitorService.registerVisit().subscribe({
      error: () => {
        this.visitorService.getVisitorCount().subscribe();
      }
    });
  }

  public getRoleLabel(role: string | undefined): string {
    if (!role) return '';
    switch (role) {
      case 'PLAYER':
      case 'ROLE_PLAYER': return 'لاعب';
      case 'ACADEMY':
      case 'ROLE_ACADEMY': return 'أكاديمية';
      case 'CLUB':
      case 'ROLE_CLUB': return 'نادي';
      default: return 'مستخدم';
    }
  }

  public getRoleClass(role: string | undefined): string {
    if (!role) return '';
    switch (role) {
      case 'PLAYER':
      case 'ROLE_PLAYER': return 'badge-player';
      case 'ACADEMY':
      case 'ROLE_ACADEMY': return 'badge-academy';
      case 'CLUB':
      case 'ROLE_CLUB': return 'badge-club';
      default: return '';
    }
  }

  public logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
