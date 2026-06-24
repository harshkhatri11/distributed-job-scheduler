import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { JobService } from '../../core/services/job.service';
import { DeadLetterEvent } from '../../core/models/dead-letter-event.model';
import { DlqDetailDialogComponent } from './dlq-detail-dialog/dlq-detail-dialog.component';
import { formatDistanceToNow } from 'date-fns';

@Component({
  selector: 'app-dlq',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatDialogModule,
  ],
  templateUrl: './dlq.component.html',
})
export class DlqComponent {
  private readonly jobService = inject(JobService);
  private readonly dialog = inject(MatDialog);

  readonly events = signal<DeadLetterEvent[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly displayedColumns = ['jobId', 'attemptsMade', 'failureReason', 'createdAt', 'actions'];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.jobService.getDlq().subscribe({
      next: (data) => {
        this.events.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Failed to load dead letter events.');
        this.loading.set(false);
      },
    });
  }

  openDetail(event: DeadLetterEvent): void {
    this.dialog.open(DlqDetailDialogComponent, {
      data: event,
      width: '720px',
      maxHeight: '85vh',
    });
  }

  truncate(text: string | null, length = 120): string {
    if (!text) return '—';
    return text.length > length ? text.slice(0, length) + '…' : text;
  }

  relativeTime(iso: string): string {
    return formatDistanceToNow(new Date(iso), { addSuffix: true });
  }

  shortId(uuid: string): string {
    return uuid.slice(0, 8) + '…';
  }
}
