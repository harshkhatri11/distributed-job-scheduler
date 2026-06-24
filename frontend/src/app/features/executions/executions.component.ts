import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { forkJoin } from 'rxjs';
import { environment } from '../../../environments/environment';
import { JobService } from '../../core/services/job.service';
import { JobExecution } from '../../core/models/job-execution.model';
import { Job } from '../../core/models/job.model';
import { StatusChipComponent } from '../../shared/components/status-chip/status-chip.component';
import { ExecutionErrorDialogComponent } from './execution-error-dialog/execution-error-dialog.component';

@Component({
  selector: 'app-executions',
  standalone: true,
  imports: [
    DatePipe,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    StatusChipComponent,
  ],
  templateUrl: './executions.component.html',
  styleUrl: './executions.component.scss',
})
export class ExecutionsComponent implements OnInit {
  private readonly jobService = inject(JobService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly zipkinBaseUrl = environment.zipkinBaseUrl;

  readonly executions = signal<JobExecution[]>([]);
  readonly job = signal<Job | null>(null);
  readonly loading = signal(false);

  readonly columns = [
    'attemptNumber',
    'status',
    'triggeredAt',
    'startedAt',
    'completedAt',
    'duration',
    'actions',
  ];

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;
    this.load(id);
  }

  load(jobId: string): void {
    this.loading.set(true);
    forkJoin({
      job: this.jobService.getJob(jobId),
      executions: this.jobService.getExecutions(jobId),
    }).subscribe({
      next: ({ job, executions }) => {
        this.job.set(job);
        this.executions.set(executions);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  getDuration(execution: JobExecution): string {
    if (!execution.startedAt || !execution.completedAt) return '—';
    const ms = new Date(execution.completedAt).getTime() - new Date(execution.startedAt).getTime();
    if (ms < 1000) return `${ms}ms`;
    return `${(ms / 1000).toFixed(1)}s`;
  }

  openTrace(execution: JobExecution): void {
    if (!execution.traceId) return;
    window.open(`${this.zipkinBaseUrl}/zipkin/traces/${execution.traceId}`, '_blank');
  }

  openError(execution: JobExecution): void {
    this.dialog.open(ExecutionErrorDialogComponent, {
      data: execution,
      width: '640px',
      maxHeight: '80vh',
    });
  }

  goBack(): void {
    this.router.navigate(['/jobs']);
  }
}
