import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { JobFormDialogComponent } from './job-form-dialog.component';
import { MatDialogRef } from '@angular/material/dialog';

describe('JobFormDialogComponent', () => {
  let fixture: ComponentFixture<JobFormDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JobFormDialogComponent],
      providers: [{ provide: MatDialogRef, useValue: { close: vi.fn() } }],
    }).compileComponents();

    fixture = TestBed.createComponent(JobFormDialogComponent);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should be invalid when empty', () => {
    expect(fixture.componentInstance.form.invalid).toBe(true);
  });
});
