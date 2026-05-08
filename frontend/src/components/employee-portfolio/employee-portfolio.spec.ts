import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmployeePortfolio } from './employee-portfolio';

describe('EmployeePortfolio', () => {
  let component: EmployeePortfolio;
  let fixture: ComponentFixture<EmployeePortfolio>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeePortfolio],
    }).compileComponents();

    fixture = TestBed.createComponent(EmployeePortfolio);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
