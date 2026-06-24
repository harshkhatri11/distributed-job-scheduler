import { Component, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { environment } from '../../../../environments/environment';
import { JobExecution } from '../../../core/models/job-execution.model';

@Component({
  selector: 'app-execution-error-dialog',
  standalone: true,
  imports: [DatePipe, MatDialogModule, MatButtonModule, MatIconModule, MatDividerModule],
  templateUrl: './execution-error-dialog.component.html',
  styleUrl: './execution-error-dialog.component.scss',
})
export class ExecutionErrorDialogComponent {
  readonly execution = inject<JobExecution>(MAT_DIALOG_DATA);
  private readonly zipkinBaseUrl = environment.zipkinBaseUrl;

  openTrace(): void {
    if (!this.execution.traceId) return;
    window.open(`${this.zipkinBaseUrl}/zipkin/traces/${this.execution.traceId}`, '_blank');
  }
}
