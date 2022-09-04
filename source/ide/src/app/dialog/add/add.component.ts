import { Component, OnInit } from '@angular/core'
import { DialogConfig } from '../dialog-config'
import { DialogRef } from '../dialog-ref'
import { InfoBoxService } from 'src/app/service/infobox.service'


@Component({
    selector: 'DialogAdd',
    templateUrl: './add.component.html',
    styleUrls: ['./add.component.scss']
})
export class DialogAdd implements OnInit {
  constructor(
    private mInfoBoxService: InfoBoxService,
    public mDialogConfig: DialogConfig,
    public mDialogRef: DialogRef) {}


  ngOnInit() {
    window.setTimeout(() => this.mInfoBoxService.mText = 'empty')
  }
}