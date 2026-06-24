export type ExecutionStatus = 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED' | 'DEAD_LETTERED';

export interface JobExecution {
  id: string;
  jobId: string;
  status: ExecutionStatus;
  attemptNumber: number;
  triggeredAt: string | null;
  startedAt: string | null;
  completedAt: string | null;
  resultOutput: string | null;
  errorMessage: string | null;
  traceId: string | null;
}
