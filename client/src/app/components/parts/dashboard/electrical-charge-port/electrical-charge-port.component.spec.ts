import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ElectricalChargePortComponent } from './electrical-charge-port.component';

describe('ElectricalChargePortComponent', () => {
  let component: ElectricalChargePortComponent;
  let fixture: ComponentFixture<ElectricalChargePortComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ElectricalChargePortComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ElectricalChargePortComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
