export interface DeadLetterEvent {
  id: string;
  jobId: string;
  executionId: string;
  payload: string;
  failureReason: string | null;
  attemptsMade: number;
  createdAt: string;
}
