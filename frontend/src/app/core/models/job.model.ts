export type JobType = 'HTTP_WEBHOOK' | 'SHELL_COMMAND';
export type JobStatus = 'ACTIVE' | 'PAUSED' | 'DISABLED';

export interface Job {
  id: string;
  name: string;
  description: string | null;
  jobType: JobType;
  cronExpression: string;
  jobConfig: Record<string, unknown>;
  status: JobStatus;
  maxRetries: number;
  timeoutSeconds: number;
  nextFireTime: string | null;
  lastFireTime: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateJobRequest {
  name: string;
  description?: string;
  jobType: JobType;
  cronExpression: string;
  jobConfig: Record<string, unknown>;
  maxRetries?: number;
  timeoutSeconds?: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
