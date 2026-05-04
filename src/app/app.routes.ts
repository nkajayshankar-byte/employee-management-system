import { Routes } from "@angular/router";
import { LoginComponent } from "../components/login/login";
import { AdminDashboardComponent } from "../components/admin-dashboard/admin-dashboard";
import { AuthGuard } from "../services/auth.guard";
import { EmployeeListComponent } from "../components/employee-list/employee-list";
import { EmployeeDetailsComponent } from "../components/employee-details/employee-details";
import { AddEmployeeComponent } from "../components/add-employee/add-employee";
import { EditEmployeeComponent } from "../components/edit-employee/edit-employee";
import { EmployeePortfolioComponent } from "../components/employee-portfolio/employee-portfolio";
import { CompanyInfoComponent } from "../components/company-info/company-info";
import { CareersComponent } from "../components/careers/careers";
import { ResetPasswordComponent } from "../components/reset-password-component/reset-password-component";
import { AdminCompanyComponent } from "../components/admin-company/admin-company";
import { AssetManagementComponent } from "../components/assetmanagement/assetmanagement";
import { AddAssetComponent } from "../components/add-asset/add-asset";
import { EditAssetComponent } from "../components/edit-asset/edit-asset";
import { AssetDetailsComponent } from "../components/asset-details/asset-details";
import { LeaveManagementComponent } from "../components/leave-management/leave-management";
import { UnsavedChangesGuard } from "../services/unsaved-changes.guard";
import { LandingPageComponent } from "../components/landing-page/landing-page";
import { ShiftManagementComponent } from "../components/shift-management/shift-management";
import { ShiftAssignmentComponent } from "../components/shift-assignment/shift-assignment";
import { AttendanceDashboardComponent } from "../components/attendance-dashboard/attendance-dashboard";



export const routes: Routes = [
  { path: '', component: LandingPageComponent },
  { path: 'login', component: LoginComponent },

  { 
    path: 'admin', 
    component: AdminDashboardComponent, 
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  { 
    path: 'admin/employees', 
    component: EmployeeListComponent, 
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  { 
    path: 'admin/employee/:id', 
    component: EmployeeDetailsComponent, 
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  { 
    path: 'admin/add-employee', 
    component: AddEmployeeComponent, 
    canActivate: [AuthGuard],
    canDeactivate: [UnsavedChangesGuard],
    data: { role: 'ADMIN' }
  },
  { 
    path: 'admin/edit-employee/:id', 
    component: EditEmployeeComponent, 
    canActivate: [AuthGuard],
    canDeactivate: [UnsavedChangesGuard],
    data: { role: 'ADMIN' }
  },
  
  { 
    path: 'employee/portfolio', 
    component: EmployeePortfolioComponent, 
    canActivate: [AuthGuard],
    canDeactivate: [UnsavedChangesGuard],
    data: { roles: ['EMPLOYEE', 'USER'] }
  },
  { 
    path: 'company-info', 
    component: CompanyInfoComponent
  },
  { 
    path: 'careers', 
    component: CareersComponent
  },
  { path: 'reset-password', 
    component: ResetPasswordComponent,
  },
  {
  path: 'admin/company',
  component: AdminCompanyComponent,
  canActivate: [AuthGuard],
  canDeactivate: [UnsavedChangesGuard]
},
  { 
    path: 'admin/assets',
    component: AssetManagementComponent,
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  { 
    path: 'admin/assets/add',
    component: AddAssetComponent,
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  { 
    path: 'admin/assets/edit/:id',
    component: EditAssetComponent,
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  { 
    path: 'admin/assets/view/:id',
    component: AssetDetailsComponent,
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  { 
    path: 'employee/assets',
    component: AssetManagementComponent,
    canActivate: [AuthGuard],
    data: { role: 'EMPLOYEE' }
  },
  { 
    path: 'employee/assets/view/:id',
    component: AssetDetailsComponent,
    canActivate: [AuthGuard],
    data: { role: 'EMPLOYEE' }
  },
  {
    path: 'admin/leaves',
    component: LeaveManagementComponent,
    canActivate: [AuthGuard],
    data: { roles: 'ADMIN' }
  },
  {
    path: 'employee/leaves',
    component: LeaveManagementComponent,
    canActivate: [AuthGuard],
    data: { roles: 'EMPLOYEE' }
  },
  {
    path: 'admin/leaves/:id',
    component: LeaveManagementComponent,
    canActivate: [AuthGuard],
    data: { roles: 'ADMIN' }
  },
  { 
    path: 'admin/shifts', 
    component: ShiftManagementComponent, 
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  { 
    path: 'admin/shift-assignment', 
    component: ShiftAssignmentComponent, 
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  { 
    path: 'admin/attendance', 
    component: AttendanceDashboardComponent, 
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  { 
    path: 'employee/attendance', 
    component: AttendanceDashboardComponent, 
    canActivate: [AuthGuard],
    data: { roles: ['EMPLOYEE', 'ADMIN'] }
  }
];