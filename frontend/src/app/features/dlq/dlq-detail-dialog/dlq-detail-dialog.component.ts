import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { DeadLetterEvent } from '../../../core/models/dead-letter-event.model';

@Component({
  selector: 'app-dlq-detail-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, MatDividerModule],
  templateUrl: './dlq-detail-dialog.component.html',
})
export class DlqDetailDialogComponent {
  readonly event = inject<DeadLetterEvent>(MAT_DIALOG_DATA);

  parsedPayload(): string {
    try {
      return JSON.stringify(JSON.parse(this.event.payload), null, 2);
    } catch {
      return this.event.payload;
    }
  }
}
