import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyRechargingPointComponent } from './my-recharging-point.component';

describe('MyRechargingPointComponent', () => {
  let component: MyRechargingPointComponent;
  let fixture: ComponentFixture<MyRechargingPointComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyRechargingPointComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MyRechargingPointComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
