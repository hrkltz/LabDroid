import { Component, AfterViewInit, HostListener, OnInit, OnDestroy } from '@angular/core'
import { WebSocketService } from '../../../service/web-socket.service'
import { ActivatedRoute, Router, NavigationCancel } from '@angular/router'
import { FormGroup, Validators, FormControl } from '@angular/forms'

import * as ace from 'ace-builds'
import 'ace-builds/src-noconflict/mode-html'
import 'ace-builds/src-noconflict/theme-github'
import 'ace-builds/src-noconflict/ext-language_tools'
import 'ace-builds/src-noconflict/ext-searchbox'
import { first, filter } from 'rxjs/operators'
import { InfoBoxService } from 'src/app/service/infobox.service'
import { ToastService } from 'src/app/toast'
import { Observable } from 'rxjs'
import { Location, KeyValue } from '@angular/common'
import { DialogService } from 'src/app/dialog/dialog.service'
import { DialogInput } from 'src/app/dialog/generic/input/input.component'
import { DialogBoolean } from 'src/app/dialog/generic/boolean/boolean.component'
import { DialogUnsaved } from 'src/app/dialog/unsaved/unsaved.component'


@Component({
  selector: 'chmlnProjectFrontendView',
  templateUrl: './frontend.view.html',
  styleUrls: ['./frontend.view.scss']
})
export class FrontendView implements OnInit, AfterViewInit, OnDestroy {
  public mProjectName: string
  public mFormGroup = new FormGroup({
    code: new FormControl('', [Validators.maxLength(10000)])
  })
  
  public mSelection = {
    isFile: false,
    path: null
  }
  public mEditor: any
  public mOriginal: any = null
  public mInputsCountOriginal: number = 0
  public mOutputsCountOriginal: number = 0
  public mModifications: Array<any> = []


  

  // keydown: avoid default action for ctrl+s
  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent): boolean {
    if (event.ctrlKey) {
      switch (event.key) {
        case 'd':
          this.delete()
			    return( false )
        case 'r':
          this.reset()
			    return( false )
        case 's':
          this.save()
			    return( false )
      }
    }
  }


  // @HostListener allows us to also guard against browser refresh, close, etc.
  @HostListener('window:beforeunload')
  canDeactivate(): Observable<boolean> | boolean {
    return !this.mFormGroup.dirty
  }

  public mTree: Object = {};

  constructor(
    pActivatedRoute: ActivatedRoute,
    private mToastService: ToastService,
    private mRouter: Router,
    private mLocation: Location,
    private mDialogService: DialogService,
    public mWebSocketService: WebSocketService,
    public mInfoBoxService: InfoBoxService) {
      this.mWebSocketService.mShowSide = true

      this.mProjectName = pActivatedRoute.snapshot.paramMap.get('projectName')
  }


  ngOnInit() {
    this.mInfoBoxService.mText = 'empty'
    
    // This is a workaround to solve https://github.com/angular/angular/issues/16314
    this.mRouter.events.pipe(filter((event) => event instanceof NavigationCancel)).subscribe(
      () => this.mLocation.replaceState('')
    )

    this.load()
  }


  ngAfterViewInit() {
    ace.config.set('basePath', '/ace-builds/src-noconflict');
    ace.config.set('modePath', '/ace-builds/src-noconflict');
    ace.config.set('themePath', '/ace-builds/src-noconflict');

    this.mEditor = ace.edit('editor', {
      highlightActiveLine: true,
      fontFamily: "Roboto Mono"
    });

    this.mEditor.setTheme("ace/theme/github"); //"ace/theme/crimson_editor"
    this.mEditor.getSession().setMode("ace/mode/html");
    this.mEditor.getSession().setUseWorker(false);
    this.mEditor.setReadOnly(false);

    this.mEditor.getSession().on('change', () => {
      this.mFormGroup.controls.code.setValue(this.mEditor.getValue());
      this.mFormGroup.controls.code.markAsDirty();
    });

    this.mEditor.focus()
  }


  ngOnDestroy() {
    this.mInfoBoxService.mText = 'empty'
  }


  public load() {
    this.mWebSocketService.File2.load$.pipe(first()).subscribe((pResult) => {
      this.mTree = JSON.parse(pResult)
    })

    this.mWebSocketService.File2.load(this.mProjectName, "Frontend/")
  }


  public selectGuard(path: string, isFile: boolean) {
    if (this.mFormGroup.dirty) {

      this.mDialogService.open(DialogUnsaved, {}).afterClosedSimple.pipe(first()).subscribe((pResult) => {
        if (pResult) 
          this.select(path, isFile)
      });
    } else
      this.select(path, isFile)
  }


  private select(path: string, isFile: boolean) {
    this.mSelection = {
      'isFile': isFile,
      'path': path
    }

    if (this.mSelection.isFile) {
      this.reset()
      this.mInfoBoxService.mText = 'frontend-file'
    } else
      this.mInfoBoxService.mText = 'frontend-folder'
  }


  public createFile() {
    let lPath: string = ''

    if (this.mSelection.path !== null) 
      lPath = this.mSelection.path

    if (this.mSelection.isFile) {
      let lastItem = lPath.split('/')
      lastItem.pop()
      lPath = lastItem.join('/')
    }

    const ref = this.mDialogService.open(DialogInput, {
      'data': {
        'header': 'Create File'
      }
    })

    ref.afterClosed.subscribe((ppResult) => {
      if (ppResult.success) {
        this.mWebSocketService.File2.create$.pipe(first()).subscribe((ppResult) => {
          if (ppResult)
            this.load()
        })

        this.mWebSocketService.File2.create(this.mProjectName, `Frontend/${lPath}/${ppResult.value}`)
      }
    })
  }


  public createFolder() {
    // TODO check for unsaved changes
    let lPath: string = ''

    if (this.mSelection.path !== null) 
      lPath = this.mSelection.path

    if (this.mSelection.isFile) {
      let lastItem = lPath.split('/')
      lastItem.pop()
      lPath = lastItem.join('/')
    }

    const ref = this.mDialogService.open(DialogInput, {
      'data': {
        'header': 'Create Folder'
      }
    })

    ref.afterClosed.subscribe((ppResult) => {
      if (ppResult.success) {
        this.mWebSocketService.File2.createFolder$.pipe(first()).subscribe((ppResult) => {
          if (ppResult)
            this.load()
            // TODO select created file
        })

        this.mWebSocketService.File2.createFolder(this.mProjectName, `Frontend/${lPath}/${ppResult.value}`)
      }
    })
  }

  public save(): void {
    this.mWebSocketService.File2.save$.pipe(first()).subscribe((eSuccess) => {
      if (eSuccess) {
        this.mFormGroup.controls.code.markAsPristine()
        
        this.mToastService.show({
          type: 'success',
          text: 'Saved'
        })
      } else
        this.mToastService.show({
          type: 'error',
          text: 'Couldn\'t save'
        })
    })

    this.mWebSocketService.File2.save(this.mProjectName, `Frontend/${this.mSelection.path}`, this.mFormGroup.controls.code.value)
  }


  public delete(): void {
    this.mWebSocketService.File2.delete$.pipe(first()).subscribe((pResult) => {
      if (pResult) {
        this.mToastService.show({
          type: 'success',
          text: 'Deleted'
        })

        this.mSelection = {
          isFile: false,
          path: null
        }
        
        this.load()
      } else {
        this.mToastService.show({
          type: 'error',
          text: 'Deleted'
        })
      }
    })
    this.mWebSocketService.File2.delete(this.mProjectName, `Frontend/${this.mSelection.path}`)
  }


  public reset(): void {
    this.mWebSocketService.File2.load$.pipe(first()).subscribe((pResult) => {
      this.mFormGroup.controls.code.setValue(JSON.parse(pResult))
      /*this.mToastService.show({
        type: 'success',
        text: 'Reloaded'
      })*/

      this.mEditor.setValue(this.mFormGroup.controls.code.value, -1)

      this.mFormGroup.controls.code.markAsPristine() // needs to be after this.mEditor.setValue(..)
    })
    this.mWebSocketService.File2.load(this.mProjectName, `Frontend/${this.mSelection.path}`)
  }


  public customOrder(a: KeyValue<number, object>, b: KeyValue<number, object>): number {
    return a.value != null ? 1 : 0;
  }
}