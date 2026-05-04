import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminCompany } from './admin-company';

describe('AdminCompany', () => {
  let component: AdminCompany;
  let fixture: ComponentFixture<AdminCompany>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminCompany],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminCompany);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
