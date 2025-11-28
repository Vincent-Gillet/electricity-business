import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyRechargingPointsComponent } from './my-recharging-points.component';

describe('MyRechargingPointsComponent', () => {
  let component: MyRechargingPointsComponent;
  let fixture: ComponentFixture<MyRechargingPointsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyRechargingPointsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MyRechargingPointsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
