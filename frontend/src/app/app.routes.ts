import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'overview',
    pathMatch: 'full',
  },
  {
    path: '',
    loadComponent: () => import('./layout/shell/shell.component').then((m) => m.ShellComponent),
    children: [
      {
        path: 'overview',
        loadComponent: () =>
          import('./features/overview/overview.component').then((m) => m.OverviewComponent),
      },
      {
        path: 'jobs',
        loadComponent: () => import('./features/jobs/jobs.component').then((m) => m.JobsComponent),
      },
      {
        path: 'jobs/:id/executions',
        loadComponent: () =>
          import('./features/executions/executions.component').then((m) => m.ExecutionsComponent),
      },
      {
        path: 'dlq',
        loadComponent: () => import('./features/dlq/dlq.component').then((m) => m.DlqComponent),
      },
    ],
  },
  {
    path: '**',
    redirectTo: 'overview',
  },
];
