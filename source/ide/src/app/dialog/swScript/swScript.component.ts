import { Component, AfterViewInit, HostListener } from '@angular/core'
import { DialogConfig } from '../dialog-config'
import { DialogRef } from '../dialog-ref'
import { FormGroup, FormControl, Validators } from '@angular/forms'

import * as ace from 'ace-builds'
import 'ace-builds/src-noconflict/mode-javascript'
import 'ace-builds/src-noconflict/theme-github'
import 'ace-builds/src-noconflict/ext-language_tools'
import 'ace-builds/src-noconflict/ext-searchbox'
import { InfoBoxService } from 'src/app/service/infobox.service'


@Component({
    selector: 'DialogSwScript',
    templateUrl: './swScript.component.html',
    styleUrls: ['./swScript.component.scss']
})
export class DialogSwScript implements AfterViewInit {
  public mFormGroup = new FormGroup({
    code: new FormControl([Validators.maxLength(1000)])
  })

  public mEditor: any // ace.edit("editor")
  public mOriginal: any = null
  public mInputsCountOriginal: number = 0
  public mOutputsCountOriginal: number = 0
  public mModifications: Array<any> = []


  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if (event.ctrlKey) {
      switch (event.key) {
        case 'r':
          this.reset()
			    return( false )
        case 's':
          this.save()
			    return( false )
      }
    } else {
      switch (event.key) {
        case 'Escape':
          this.mDialogRef.close(false)
			    return( false )
      }
    }
  }


  constructor(
    private mInfoBoxService: InfoBoxService,
    public mDialogConfig: DialogConfig, 
    public mDialogRef: DialogRef) {
    this.mOriginal = this.mDialogConfig.data.node
    this.mFormGroup.controls.code.setValue(this.mOriginal.code)
  }


  ngAfterViewInit() {
    ace.config.set('basePath', '/ace-builds/src-noconflict')
    ace.config.set('modePath', '/ace-builds/src-noconflict')
    ace.config.set('themePath', '/ace-builds/src-noconflict')

    this.mEditor = ace.edit('editor', {
      highlightActiveLine: true,
      fontFamily: "Roboto Mono"//,
    })

    this.mEditor.setTheme("ace/theme/github") //"ace/theme/crimson_editor"
    this.mEditor.getSession().setMode("ace/mode/javascript")
    this.mEditor.getSession().setUseWorker(false)
    this.mEditor.setReadOnly(false)

    this.mEditor.setValue(this.mFormGroup.controls.code.value, -1)

    // TODO send from back end
    var wordList = [
      {'word': '$append(name, value)', 'meta': 'string, string'},
      {'word': '$delete(name)', 'meta': 'string'},
      {'word': '$load(name)', 'meta': 'string'},
      {'word': '$check(name)', 'meta': 'string'},
      {'word': '$save(name, value)', 'meta': 'string, string'},
      {"word": "$sleep(time)", "meta":"number [ms]"},
      {"word": "$in(port)", "meta":"number"},
      {"word": "$out(port, object)", "meta":"number, any"},
      {"word": "JSON.stringify(object)", "meta":"any"},
      {"word": "JSON.parse(json)", "meta":"string"},
      {"word": "Math.PI", "meta":""},
      {"word": "Math.round(x)", "meta":"number"},
      {"word": "Math.pow(x, y)", "meta":"number, number"},
      {"word": "Math.sqrt(x)", "meta":"number"},
      {"word": "Math.abs(x)", "meta":"number"},
      {"word": "Math.sin(x)", "meta":"number"},
      {"word": "Math.cos(x)", "meta":"number"},
      {"word": "Math.random()", "meta":""},
    ]
    
    let rhymeCompleter = {
        getCompletions: function(editor, session, pos, prefix, callback) {
            if (prefix.length === 0) { callback(null, []); return }

            callback(null, wordList.map(function(ea) {
              return {name: ea.word, value: ea.word, meta: ea.meta}
            }))
        }
    }

    let langTools = ace.require("ace/ext/language_tools")
    langTools.setCompleters([rhymeCompleter]) // this also removes the standard locale variable completor

    this.mEditor.setOptions({
      enableBasicAutocompletion: true,
      enableSnippets: false,
      enableLiveAutocompletion: true
    })

    this.mEditor.getSession().on('change', () => {
      this.mFormGroup.controls.code.setValue(this.mEditor.getValue())
      this.mFormGroup.controls.code.markAsDirty()
    })

    this.mEditor.focus()

    window.setTimeout(() => this.mInfoBoxService.mText = "dialogScript", 0)
  }


  ngOnDestroy() {
    this.mInfoBoxService.mText = "empty"
  }


  public save(): void {
    this.mDialogRef.close(true, this.mFormGroup.controls.code.value)
  }


  public reset(): void {
    this.mEditor.setValue(this.mOriginal.code, -1)
    
    this.mFormGroup.reset({
      code: this.mOriginal.code
    })
  }


  public cancel(): void {
    this.mDialogRef.close(false)
  }
}