import { Component, inject } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { CreateJobRequest, JobType } from '../../../core/models/job.model';

@Component({
  selector: 'app-job-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './job-form-dialog.component.html',
  styleUrl: './job-form-dialog.component.scss',
})
export class JobFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<JobFormDialogComponent>);

  readonly jobTypes: JobType[] = ['HTTP_WEBHOOK', 'SHELL_COMMAND'];

  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: [''],
    jobType: ['HTTP_WEBHOOK' as JobType, Validators.required],
    cronExpression: ['', Validators.required],
    url: ['', Validators.required],
    method: ['POST', Validators.required],
    body: [''],
    maxRetries: [3, [Validators.required, Validators.min(0), Validators.max(10)]],
    timeoutSeconds: [30, [Validators.required, Validators.min(5), Validators.max(3600)]],
  });

  get isHttpWebhook(): boolean {
    return this.form.get('jobType')?.value === 'HTTP_WEBHOOK';
  }

  submit(): void {
    if (this.form.invalid) return;

    const v = this.form.getRawValue();

    const jobConfig: Record<string, unknown> = this.isHttpWebhook
      ? { url: v.url, method: v.method, ...(v.body ? { body: v.body } : {}) }
      : { command: v.url };

    const request: CreateJobRequest = {
      name: v.name!,
      description: v.description ?? undefined,
      jobType: v.jobType!,
      cronExpression: v.cronExpression!,
      jobConfig,
      maxRetries: v.maxRetries!,
      timeoutSeconds: v.timeoutSeconds!,
    };

    this.dialogRef.close(request);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
