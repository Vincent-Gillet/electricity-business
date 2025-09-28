import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MapTerminalComponent } from './map-terminal.component';

describe('MapTerminalComponent', () => {
  let component: MapTerminalComponent;
  let fixture: ComponentFixture<MapTerminalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MapTerminalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MapTerminalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
