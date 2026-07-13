import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Employee, EmployeeService } from '../../services/employee';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-employee-details',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './employee-details.html',
  styleUrls: ['./employee-details.css']
})
export class EmployeeDetailsComponent implements OnInit {
  employee: Employee | null = null;
  loading = false;
  apiUrl = environment.apiUrl;

  constructor(
    private route: ActivatedRoute,
    private employeeService: EmployeeService,
    private router: Router,
    private toastr: ToastrService,
    private cd: ChangeDetectorRef  
  ) { }

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    this.loadEmployee(id);
  }

  loadEmployee(id: string): void {
  this.loading = true;

  this.employeeService.getEmployeeById(id).subscribe({
    next: (data) => {
      console.log('Employee Details:', data);
      this.employee = data;
      this.loading = false;
      this.cd.detectChanges(); 
    },
    error: (err) => {
      console.error(err);
      this.loading = false;
      this.cd.detectChanges(); 
    }
  });
}

  goBack(): void {
    this.router.navigate(['/admin/employees']);
  }
}