import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DlqComponent } from './dlq.component';

describe('DlqComponent', () => {
  let fixture: ComponentFixture<DlqComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DlqComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DlqComponent);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });
});
