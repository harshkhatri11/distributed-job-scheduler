import { Component, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatTooltipModule } from '@angular/material/tooltip';
import { environment } from '../../../environments/environment';

interface NavItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-sidenav',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, MatTooltipModule],
  templateUrl: './sidenav.component.html',
  styleUrl: './sidenav.component.scss',
})
export class SidenavComponent {
  readonly grafanaUrl = environment.grafanaBaseUrl;
  readonly zipkinUrl = environment.zipkinBaseUrl;
  readonly navItemClicked = output<void>();

  readonly navItems: NavItem[] = [
    { label: 'Overview', icon: 'dashboard', route: '/overview' },
    { label: 'Jobs', icon: 'work', route: '/jobs' },
    { label: 'Dead Letter', icon: 'error_outline', route: '/dlq' },
  ];

  onNavClick(): void {
    this.navItemClicked.emit();
  }
}
