import { Component, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { DialogConfig } from '../../dialog-config';
import { DialogRef } from '../../dialog-ref';
import { FormGroup, FormControl } from '@angular/forms';
import { MatFormField } from '@angular/material/form-field';

@Component({
    selector: 'dialogSelect',
    templateUrl: './select.component.html',
    styleUrls: ['./select.component.scss']
})
export class DialogSelect implements AfterViewInit {
  @ViewChild('select', { static: true }) mSelect: MatFormField
  public mHeader: string = null
  public mValues: Array<string> = null

  public mFormGroup = new FormGroup({
    name: new FormControl(null),
    select: new FormControl(null)
  })


  constructor(
    public mDialogRef: DialogRef,
    pDialogConfig: DialogConfig) {
      this.mHeader = pDialogConfig.data.header
      this.mValues = pDialogConfig.data.values
     }


  public ngAfterViewInit(): void {
    window.setTimeout(() => {
      // Defer this code to another Javascript Virtual Machine turn.
      // Source: https://blog.angular-university.io/angular-debugging/
      this.mFormGroup.controls.select.setValue(this.mValues[0])
      this.mSelect._elementRef.nativeElement.focus()
    });
  }
}
