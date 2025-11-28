import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyRechargingPointFormComponent } from './my-recharging-point-form.component';

describe('MyRechargingPointFormComponent', () => {
  let component: MyRechargingPointFormComponent;
  let fixture: ComponentFixture<MyRechargingPointFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyRechargingPointFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MyRechargingPointFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
