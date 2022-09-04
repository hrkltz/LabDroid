import { Component, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { DialogConfig } from '../../dialog-config';
import { DialogRef } from '../../dialog-ref';
import { FormGroup, FormControl, Validators } from '@angular/forms';

@Component({
    selector: 'dialogInput',
    templateUrl: './input.component.html',
    styleUrls: ['./input.component.scss']
})
export class DialogInput implements AfterViewInit {
  @ViewChild('input', { static: true }) mInput: ElementRef;
  public mHeader: string = null;
  public mRegExp: RegExp = null;
  public mValue: string = '';

  public mFormGroup = new FormGroup({
    name: new FormControl(null)
  })


  constructor(
    public mDialogRef: DialogRef,
    pDialogConfig: DialogConfig) {
      this.mHeader = pDialogConfig.data.header

      if (pDialogConfig.data.value)
        this.mValue = pDialogConfig.data.value

      this.mFormGroup.controls.name.setValue(this.mValue);
      this.mRegExp = pDialogConfig.data.regExp
     }


  public ngAfterViewInit(): void {
    window.setTimeout(() => {
      // Defer this code to another Javascript Virtual Machine turn.
      // Source: https://blog.angular-university.io/angular-debugging/
      this.mInput.nativeElement.focus();
    });
  }
}
