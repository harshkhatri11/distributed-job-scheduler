import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ExecutionsComponent } from './executions.component';
import { JobService } from '../../core/services/job.service';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { of } from 'rxjs';

describe('ExecutionsComponent', () => {
  let fixture: ComponentFixture<ExecutionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExecutionsComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => 'test-job-id' } } },
        },
        {
          provide: JobService,
          useValue: { getExecutions: () => of([]) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ExecutionsComponent);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should load executions on init', () => {
    expect(fixture.componentInstance.executions()).toEqual([]);
  });

  it('should return — for missing duration', () => {
    const result = fixture.componentInstance.getDuration({
      id: '1',
      jobId: '1',
      status: 'PENDING',
      attemptNumber: 1,
      triggeredAt: null,
      startedAt: null,
      completedAt: null,
      resultOutput: null,
      errorMessage: null,
      traceId: null,
    });
    expect(result).toBe('—');
  });
});
