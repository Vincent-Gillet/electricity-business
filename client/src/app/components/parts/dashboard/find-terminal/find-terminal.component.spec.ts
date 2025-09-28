import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FindTerminalComponent } from './find-terminal.component';

describe('FindTerminalComponent', () => {
  let component: FindTerminalComponent;
  let fixture: ComponentFixture<FindTerminalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FindTerminalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FindTerminalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
