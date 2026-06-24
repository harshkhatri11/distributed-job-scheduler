import { Component, inject, signal, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DatePipe } from '@angular/common';
import { JobService } from '../../core/services/job.service';
import { JobFormDialogComponent } from './job-form-dialog/job-form-dialog.component';
import { StatusChipComponent } from '../../shared/components/status-chip/status-chip.component';
import { CreateJobRequest, Job } from '../../core/models/job.model';

@Component({
  selector: 'app-jobs',
  standalone: true,
  imports: [
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatTooltipModule,
    DatePipe,
    StatusChipComponent,
  ],
  templateUrl: './jobs.component.html',
  styleUrl: './jobs.component.scss',
})
export class JobsComponent implements OnInit {
  private readonly jobService = inject(JobService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  readonly jobs = signal<Job[]>([]);
  readonly loading = signal(false);

  readonly columns = [
    'name',
    'jobType',
    'cronExpression',
    'status',
    'lastFireTime',
    'nextFireTime',
    'actions',
  ];

  ngOnInit(): void {
    this.loadJobs();
  }

  loadJobs(): void {
    this.loading.set(true);
    this.jobService.getJobs().subscribe({
      next: (jobs) => {
        this.jobs.set(jobs);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  openCreateDialog(): void {
    const ref = this.dialog.open(JobFormDialogComponent, { width: '580px' });
    ref.afterClosed().subscribe((request: CreateJobRequest | undefined) => {
      if (!request) return;
      this.jobService.createJob(request).subscribe({
        next: () => {
          this.snackBar.open('Job created', 'Dismiss', { duration: 3000 });
          this.loadJobs();
        },
      });
    });
  }

  pauseJob(job: Job): void {
    this.jobService.pauseJob(job.id).subscribe({
      next: () => {
        this.snackBar.open('Job paused', 'Dismiss', { duration: 3000 });
        this.loadJobs();
      },
    });
  }

  resumeJob(job: Job): void {
    this.jobService.resumeJob(job.id).subscribe({
      next: () => {
        this.snackBar.open('Job resumed', 'Dismiss', { duration: 3000 });
        this.loadJobs();
      },
    });
  }

  deleteJob(job: Job): void {
    this.jobService.deleteJob(job.id).subscribe({
      next: () => {
        this.snackBar.open('Job deleted', 'Dismiss', { duration: 3000 });
        this.loadJobs();
      },
    });
  }

  viewExecutions(job: Job): void {
    this.router.navigate(['/jobs', job.id, 'executions']);
  }
}
