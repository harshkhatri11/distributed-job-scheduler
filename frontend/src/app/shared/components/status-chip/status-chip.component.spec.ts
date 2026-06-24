import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StatusChipComponent } from './status-chip.component';
import { Component } from '@angular/core';

@Component({
  standalone: true,
  imports: [StatusChipComponent],
  template: `<app-status-chip status="ACTIVE" />`,
})
class TestHostComponent {}

describe('StatusChipComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render status text', () => {
    const chip = fixture.nativeElement.querySelector('.chip');
    expect(chip.textContent.trim()).toBe('ACTIVE');
  });

  it('should apply active class', () => {
    const chip = fixture.nativeElement.querySelector('.chip');
    expect(chip.classList).toContain('chip--active');
  });
});
