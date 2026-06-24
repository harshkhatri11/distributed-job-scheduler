import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { forkJoin, interval, Subscription } from 'rxjs';
import { startWith } from 'rxjs/operators';
import { NgxEchartsDirective, provideEchartsCore } from 'ngx-echarts';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatButtonModule } from '@angular/material/button';
import { JobService } from '../../core/services/job.service';
import { Job } from '../../core/models/job.model';
import { JobExecution, ExecutionStatus } from '../../core/models/job-execution.model';
import { DeadLetterEvent } from '../../core/models/dead-letter-event.model';
import * as echarts from 'echarts';

interface StatCard {
  label: string;
  value: string | number;
  sub: string;
  icon: string;
  accent: string; // css variable name
}

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [
    CommonModule,
    NgxEchartsDirective,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatButtonModule,
  ],
  providers: [provideEchartsCore({ echarts })],
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.scss',
})
export class OverviewComponent implements OnInit, OnDestroy {
  private readonly jobService = inject(JobService);
  private readonly router = inject(Router);
  readonly Math = Math;

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly lastRefreshed = signal<Date | null>(null);

  private jobs: Job[] = [];
  protected executions: JobExecution[] = [];
  private dlq: DeadLetterEvent[] = [];
  private refreshSub?: Subscription;

  readonly statCards = signal<StatCard[]>([]);

  readonly donutOptions = signal<any>(null);
  readonly lineOptions = signal<any>(null);
  readonly barOptions = signal<any>(null);

  private readonly CHART_THEME = {
    text: '#f1f5f9',
    muted: '#94a3b8',
    surface2: '#1a1e25',
    border: '#2d3340',
    accent: '#3b82f6',
    accentDim: '#1d4ed8',
  } as const;

  ngOnInit(): void {
    this.refreshSub = interval(30_000)
      .pipe(startWith(0))
      .subscribe(() => this.loadAll());
  }

  ngOnDestroy(): void {
    this.refreshSub?.unsubscribe();
  }

  loadAll(): void {
    this.error.set(null);
    forkJoin({
      jobs: this.jobService.getJobs(),
      dlq: this.jobService.getDlq(),
    }).subscribe({
      next: ({ jobs, dlq }) => {
        this.jobs = jobs;
        this.dlq = dlq;

        if (jobs.length === 0) {
          this.executions = [];
          this.build();
          this.loading.set(false);
          return;
        }

        forkJoin(jobs.map((j) => this.jobService.getExecutions(j.id))).subscribe({
          next: (results) => {
            this.executions = results.flat();
            this.build();
            this.loading.set(false);
            this.lastRefreshed.set(new Date());
          },
          error: () => {
            this.error.set('Failed to load execution data.');
            this.loading.set(false);
          },
        });
      },
      error: () => {
        this.error.set('Failed to load overview data.');
        this.loading.set(false);
      },
    });
  }

  private build(): void {
    this.buildStatCards();
    this.buildDonut();
    this.buildLine();
    this.buildBar();
  }

  private buildStatCards(): void {
    const total = this.jobs.length;
    const active = this.jobs.filter((j) => j.status === 'ACTIVE').length;
    const succeeded = this.executions.filter((e) => e.status === 'SUCCESS').length;
    const failed = this.executions.filter(
      (e) => e.status === 'FAILED' || e.status === 'DEAD_LETTERED',
    ).length;
    const total_ex = succeeded + failed;
    const successRate = total_ex === 0 ? '—' : `${Math.round((succeeded / total_ex) * 100)}%`;

    this.statCards.set([
      {
        label: 'Total Jobs',
        value: total,
        sub: `${active} active`,
        icon: 'work_outline',
        accent: '--color-accent',
      },
      {
        label: 'Executions',
        value: this.executions.length,
        sub: 'all time',
        icon: 'bolt',
        accent: '--color-accent',
      },
      {
        label: 'Success Rate',
        value: successRate,
        sub: `${succeeded} succeeded`,
        icon: 'check_circle_outline',
        accent: '--color-success',
      },
      {
        label: 'Dead Lettered',
        value: this.dlq.length,
        sub: 'exhausted retries',
        icon: 'error_outline',
        accent: '--color-danger',
      },
    ]);
  }

  private buildDonut(): void {
    const counts: Record<ExecutionStatus, number> = {
      SUCCESS: 0,
      FAILED: 0,
      DEAD_LETTERED: 0,
      RUNNING: 0,
      PENDING: 0,
    };
    this.executions.forEach((e) => counts[e.status]++);

    const palette: Record<ExecutionStatus, string> = {
      SUCCESS: '#22c55e',
      FAILED: '#ef4444',
      DEAD_LETTERED: '#f59e0b',
      RUNNING: this.CHART_THEME.accent,
      PENDING: this.CHART_THEME.muted,
    };

    const data = (Object.entries(counts) as [ExecutionStatus, number][])
      .filter(([, v]) => v > 0)
      .map(([name, value]) => ({
        name,
        value,
        itemStyle: { color: palette[name as ExecutionStatus] },
      }));

    this.donutOptions.set({
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'item',
        backgroundColor: this.CHART_THEME.surface2,
        borderColor: this.CHART_THEME.border,
        textStyle: { color: this.CHART_THEME.text, fontFamily: 'IBM Plex Mono', fontSize: 12 },
        formatter: (p: any) => `${p.marker} ${p.name}: <b>${p.value}</b> (${p.percent}%)`,
      },
      legend: {
        bottom: 0,
        textStyle: { color: this.CHART_THEME.muted, fontSize: 11, fontFamily: 'IBM Plex Mono' },
        icon: 'circle',
        itemWidth: 8,
        itemHeight: 8,
      },
      series: [
        {
          type: 'pie',
          radius: ['48%', '72%'],
          center: ['50%', '42%'],
          avoidLabelOverlap: true,
          label: { show: false },
          labelLine: { show: false },
          emphasis: {
            scale: true,
            scaleSize: 6,
            label: { show: false },
          },
          data,
        },
      ],
    });
  }

  private buildLine(): void {
    const now = Date.now();
    const hours = 24;
    const buckets: Record<string, number> = {};

    for (let i = hours - 1; i >= 0; i--) {
      const t = new Date(now - i * 3_600_000);
      buckets[this.hourKey(t)] = 0;
    }

    this.executions.forEach((e) => {
      const ts = e.startedAt ?? e.triggeredAt;
      if (!ts) return;
      const d = new Date(ts);
      if (now - d.getTime() > hours * 3_600_000) return;
      const key = this.hourKey(d);
      if (key in buckets) buckets[key]++;
    });

    const xData = Object.keys(buckets);
    const yData = Object.values(buckets);

    this.lineOptions.set({
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'axis',
        backgroundColor: this.CHART_THEME.surface2,
        borderColor: this.CHART_THEME.border,
        textStyle: { color: this.CHART_THEME.text, fontFamily: 'IBM Plex Mono', fontSize: 12 },
      },
      grid: { left: 40, right: 20, top: 16, bottom: 40 },
      xAxis: {
        type: 'category',
        data: xData,
        axisLabel: {
          color: this.CHART_THEME.muted,
          fontFamily: 'IBM Plex Mono',
          fontSize: 10,
          interval: 3,
        },
        axisLine: { lineStyle: { color: this.CHART_THEME.border } },
        splitLine: { show: false },
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        axisLabel: { color: this.CHART_THEME.muted, fontFamily: 'IBM Plex Mono', fontSize: 10 },
        splitLine: { lineStyle: { color: this.CHART_THEME.border, type: 'dashed' } },
      },
      series: [
        {
          type: 'line',
          data: yData,
          smooth: true,
          symbol: 'circle',
          symbolSize: 5,
          lineStyle: { color: this.CHART_THEME.accent, width: 2 },
          itemStyle: { color: this.CHART_THEME.accent },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(59,130,246,0.25)' },
                { offset: 1, color: 'rgba(59,130,246,0)' },
              ],
            },
          },
        },
      ],
    });
  }

  private buildBar(): void {
    const countMap: Record<string, { name: string; count: number }> = {};

    this.jobs.forEach((j) => {
      countMap[j.id] = { name: j.name, count: 0 };
    });
    this.executions.forEach((e) => {
      if (countMap[e.jobId]) countMap[e.jobId].count++;
    });

    const sorted = Object.values(countMap)
      .sort((a, b) => b.count - a.count)
      .slice(0, 10);

    this.barOptions.set({
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        backgroundColor: this.CHART_THEME.surface2,
        borderColor: this.CHART_THEME.border,
        textStyle: { color: this.CHART_THEME.text, fontFamily: 'IBM Plex Mono', fontSize: 12 },
      },
      grid: { left: 140, right: 20, top: 8, bottom: 24 },
      xAxis: {
        type: 'value',
        minInterval: 1,
        axisLabel: { color: this.CHART_THEME.muted, fontFamily: 'IBM Plex Mono', fontSize: 10 },
        splitLine: { lineStyle: { color: this.CHART_THEME.border, type: 'dashed' } },
      },
      yAxis: {
        type: 'category',
        data: sorted.map((s) => s.name),
        axisLabel: {
          color: this.CHART_THEME.text,
          fontFamily: 'IBM Plex Mono',
          fontSize: 11,
          width: 120,
          overflow: 'truncate',
        },
        axisLine: { lineStyle: { color: this.CHART_THEME.border } },
      },
      series: [
        {
          type: 'bar',
          data: sorted.map((s) => s.count),
          barMaxWidth: 20,
          itemStyle: { color: this.CHART_THEME.accent, borderRadius: [0, 4, 4, 0] },
          emphasis: { itemStyle: { color: this.CHART_THEME.accentDim } },
        },
      ],
    });
  }

  private hourKey(d: Date): string {
    return `${String(d.getHours()).padStart(2, '0')}:00`;
  }

  navigateToJob(jobName: string): void {
    this.router.navigate(['/jobs']);
  }

  formatTime(d: Date): string {
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }
}
