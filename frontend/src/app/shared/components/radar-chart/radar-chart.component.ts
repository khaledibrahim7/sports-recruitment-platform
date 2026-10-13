import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-radar-chart',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="radar-chart-container text-center">
      <svg viewBox="0 0 300 300" class="radar-svg">
        <!-- Background Concentric Hexagons (20%, 40%, 60%, 80%, 100%) -->
        <polygon *ngFor="let level of [0.2, 0.4, 0.6, 0.8, 1.0]"
                 [attr.points]="getHexagonPoints(level)"
                 class="grid-polygon" />

        <!-- Axes lines from center to outer points -->
        <line *ngFor="let pt of outerPoints"
              x1="150" y1="150"
              [attr.x2]="pt.x" [attr.y2]="pt.y"
              class="grid-axis" />

        <!-- Data Polygon -->
        <polygon [attr.points]="dataPoints" class="data-polygon" />
        
        <!-- Data Circles on vertices -->
        <circle *ngFor="let pt of vertexPoints"
                [attr.cx]="pt.x" [attr.cy]="pt.y"
                r="4" class="data-point-circle" />

        <!-- Labels -->
        <g *ngFor="let label of labelsInfo">
          <text [attr.x]="label.x" [attr.y]="label.y" class="radar-label" text-anchor="middle">
            {{ label.text }} ({{ label.value }})
          </text>
        </g>
      </svg>
    </div>
  `,
  styles: [`
    .radar-chart-container {
      width: 100%;
      max-width: 300px;
      margin: 0 auto;
    }
    .radar-svg {
      width: 100%;
      height: auto;
      overflow: visible;
    }
    .grid-polygon {
      fill: none;
      stroke: rgba(255, 255, 255, 0.1);
      stroke-width: 1;
    }
    .grid-axis {
      stroke: rgba(255, 255, 255, 0.08);
      stroke-width: 1;
      stroke-dasharray: 2, 2;
    }
    .data-polygon {
      fill: rgba(16, 185, 129, 0.25);
      stroke: #10b981;
      stroke-width: 2.5;
      filter: drop-shadow(0 0 6px rgba(16, 185, 129, 0.5));
    }
    .data-point-circle {
      fill: #fbbf24;
      stroke: #ffffff;
      stroke-width: 1.5;
    }
    .radar-label {
      fill: var(--text-main, #f3f4f6);
      font-size: 11px;
      font-weight: 700;
      font-family: 'Cairo', sans-serif;
    }
  `]
})
export class RadarChartComponent implements OnChanges {
  @Input() pace: number = 75;
  @Input() shooting: number = 70;
  @Input() passing: number = 70;
  @Input() dribbling: number = 75;
  @Input() defending: number = 65;
  @Input() physicality: number = 70;

  public dataPoints: string = '';
  public outerPoints: { x: number; y: number }[] = [];
  public vertexPoints: { x: number; y: number }[] = [];
  public labelsInfo: { x: number; y: number; text: string; value: number }[] = [];

  private readonly CENTER_X = 150;
  private readonly CENTER_Y = 150;
  private readonly RADIUS = 105;

  private readonly LABELS = [
    { key: 'pace', text: 'السرعة' },
    { key: 'shooting', text: 'التسديد' },
    { key: 'passing', text: 'التمرير' },
    { key: 'dribbling', text: 'المراوغة' },
    { key: 'defending', text: 'الدفاع' },
    { key: 'physicality', text: 'القوة' }
  ];

  ngOnChanges(changes: SimpleChanges): void {
    this.calculateRadar();
  }

  private calculateRadar(): void {
    const values = [
      Math.min(100, Math.max(10, this.pace || 75)),
      Math.min(100, Math.max(10, this.shooting || 70)),
      Math.min(100, Math.max(10, this.passing || 70)),
      Math.min(100, Math.max(10, this.dribbling || 75)),
      Math.min(100, Math.max(10, this.defending || 65)),
      Math.min(100, Math.max(10, this.physicality || 70))
    ];

    this.outerPoints = [];
    this.vertexPoints = [];
    this.labelsInfo = [];
    const pointsArr: string[] = [];

    for (let i = 0; i < 6; i++) {
      const angle = (Math.PI / 3) * i - Math.PI / 2; // start from top
      const outerX = this.CENTER_X + this.RADIUS * Math.cos(angle);
      const outerY = this.CENTER_Y + this.RADIUS * Math.sin(angle);
      this.outerPoints.push({ x: outerX, y: outerY });

      const valNorm = values[i] / 100;
      const dataX = this.CENTER_X + (this.RADIUS * valNorm) * Math.cos(angle);
      const dataY = this.CENTER_Y + (this.RADIUS * valNorm) * Math.sin(angle);
      this.vertexPoints.push({ x: dataX, y: dataY });
      pointsArr.push(`${dataX},${dataY}`);

      // Labels position slightly outside outer radius
      const labelRadius = this.RADIUS + 22;
      const labelX = this.CENTER_X + labelRadius * Math.cos(angle);
      const labelY = this.CENTER_Y + labelRadius * Math.sin(angle) + 4;
      this.labelsInfo.push({
        x: labelX,
        y: labelY,
        text: this.LABELS[i].text,
        value: values[i]
      });
    }

    this.dataPoints = pointsArr.join(' ');
  }

  public getHexagonPoints(scale: number): string {
    const points: string[] = [];
    for (let i = 0; i < 6; i++) {
      const angle = (Math.PI / 3) * i - Math.PI / 2;
      const x = this.CENTER_X + (this.RADIUS * scale) * Math.cos(angle);
      const y = this.CENTER_Y + (this.RADIUS * scale) * Math.sin(angle);
      points.push(`${x},${y}`);
    }
    return points.join(' ');
  }
}
