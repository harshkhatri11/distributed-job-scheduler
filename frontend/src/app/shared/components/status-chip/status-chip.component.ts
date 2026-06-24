import { Component, input } from '@angular/core';
import { JobStatus } from '../../../core/models/job.model';
import { ExecutionStatus } from '../../../core/models/job-execution.model';

type ChipStatus = JobStatus | ExecutionStatus;

@Component({
  selector: 'app-status-chip',
  standalone: true,
  imports: [],
  templateUrl: './status-chip.component.html',
  styleUrl: './status-chip.component.scss',
})
export class StatusChipComponent {
  readonly status = input.required<ChipStatus>();

  get chipClass(): string {
    const map: Record<ChipStatus, string> = {
      ACTIVE: 'chip--active',
      PAUSED: 'chip--paused',
      DISABLED: 'chip--disabled',
      PENDING: 'chip--pending',
      RUNNING: 'chip--running',
      SUCCESS: 'chip--success',
      FAILED: 'chip--failed',
      DEAD_LETTERED: 'chip--dead',
    };
    return map[this.status()] ?? '';
  }
}
