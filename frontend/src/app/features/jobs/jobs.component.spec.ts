import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { JobsComponent } from './jobs.component';
import { JobService } from '../../core/services/job.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { of } from 'rxjs';

describe('JobsComponent', () => {
  let fixture: ComponentFixture<JobsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JobsComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        { provide: MatDialog, useValue: { open: vi.fn() } },
        { provide: MatSnackBar, useValue: { open: vi.fn() } },
        { provide: JobService, useValue: { getJobs: () => of([]) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(JobsComponent);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should load jobs on init', () => {
    expect(fixture.componentInstance.jobs()).toEqual([]);
  });
});
