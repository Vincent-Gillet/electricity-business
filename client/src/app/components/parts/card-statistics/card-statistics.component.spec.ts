import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CardStatisticsComponent } from './card-statistics.component';

describe('CardStatisticsComponent', () => {
  let component: CardStatisticsComponent;
  let fixture: ComponentFixture<CardStatisticsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CardStatisticsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CardStatisticsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
