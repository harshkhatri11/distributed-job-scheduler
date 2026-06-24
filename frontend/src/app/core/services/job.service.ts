import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Job, CreateJobRequest } from '../models/job.model';
import { JobExecution } from '../models/job-execution.model';
import { DeadLetterEvent } from '../models/dead-letter-event.model';

@Injectable({ providedIn: 'root' })
export class JobService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  getJobs(): Observable<Job[]> {
    return this.http.get<Job[]>(`${this.base}/jobs`);
  }

  getJob(id: string): Observable<Job> {
    return this.http.get<Job>(`${this.base}/jobs/${id}`);
  }

  createJob(request: CreateJobRequest): Observable<Job> {
    return this.http.post<Job>(`${this.base}/jobs`, request);
  }

  pauseJob(id: string): Observable<Job> {
    return this.http.patch<Job>(`${this.base}/jobs/${id}/pause`, {});
  }

  resumeJob(id: string): Observable<Job> {
    return this.http.patch<Job>(`${this.base}/jobs/${id}/resume`, {});
  }

  deleteJob(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/jobs/${id}`);
  }

  getExecutions(jobId: string): Observable<JobExecution[]> {
    return this.http.get<JobExecution[]>(`${this.base}/jobs/${jobId}/executions`);
  }

  getDlq(): Observable<DeadLetterEvent[]> {
    return this.http.get<DeadLetterEvent[]>(`${this.base}/dlq`);
  }
}
