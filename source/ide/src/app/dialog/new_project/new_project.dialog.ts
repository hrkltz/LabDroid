import { Component, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { DialogConfig } from '../dialog-config';
import { DialogRef } from '../dialog-ref';
import { FormGroup, FormControl, Validators, ValidatorFn, AbstractControl } from '@angular/forms';


@Component({
  selector: 'dialogNewProject',
  templateUrl: './new_project.dialog.html',
  styleUrls: ['./new_project.dialog.scss']
})
export class DialogNewProject implements AfterViewInit {
  @ViewChild('name', { static: true }) mName: ElementRef;

  public mFormGroup = new FormGroup({
    name: new FormControl(null, [Validators.required, Validators.maxLength(25),
    this.notExists()])
  });

  public mExamples: Array<string>
  public mProjects: Array<string>
  public mSelected: string = "Empty"

  constructor(
    public mDialogConfig: DialogConfig,
    public mDialogRef: DialogRef) {
      this.mExamples = this.mDialogConfig.data.examples;
      this.mProjects = this.mDialogConfig.data.projects;
    }


  ngAfterViewInit() {
    window.setTimeout(() => {
      // Defer this code to another Javascript Virtual Machine turn.
      // Source: https://blog.angular-university.io/angular-debugging/
      this.mName.nativeElement.focus();
    });
  }


  private notExists(): ValidatorFn {
    return (pAbastractControl: AbstractControl): { [key: string]: any } | null => {
      if (this.mProjects) {
        if (this.mProjects.find((value: string, index: number) => {
          return value == pAbastractControl.value;
        })) {
          return pAbastractControl.value
        }
      }

      return null;
    };
  }

  public save(): void {
    this.mDialogRef.close(true, {
      name: this.mFormGroup.controls.name.value,
      example: this.mSelected
    })
  }
}
