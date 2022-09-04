import { Component, Input, OnChanges, AfterViewInit, ViewChild, ElementRef, SimpleChange } from '@angular/core';
import { API_STORAGE } from '../service/rest.service';

declare var ace: any;


@Component({
  selector: 'app-ace',
  templateUrl: './ace.component.html',
  styleUrls: ['./ace.component.scss']
})
export class AceComponent implements AfterViewInit, OnChanges {
  @Input('src') src: string;

  @ViewChild('editor') mEditorElement: ElementRef;

  mAce: any; // ace.edit("editor");

  mAceSession: any;

  mAceDocument: any;

  mAceUndoManager: any;


  constructor(private mApiStorage: API_STORAGE) { }

  ngOnChanges(changes: {[propKey: string]: SimpleChange}) {
    for (let propName in changes) {
      let changedProp = changes[propName];
      let to = JSON.stringify(changedProp.currentValue);
      switch(propName) {
        case 'src':
          if (changedProp.isFirstChange()) {
            this.load(to);
          } else { 
            let from = JSON.stringify(changedProp.previousValue); 

            if (to.length === 2) {
              // console.log("NO file was loaded!");
            } else if(this.src.split('/')[2] === 'script') {
              // console.log("SCRIPT file was loaded!");
              this.mAce.setReadOnly(false);
              if(this.mAce.container.childNodes.length === 7)
                this.mAce.container.removeChild(this.mAce.container.childNodes[6]);
              this.load(to);
            } else {
              // console.log('EDIT file was loaded!');
              this.mAce.setReadOnly(false);

              if (this.mAce.container.childNodes.length === 7) {
                this.mAce.container.removeChild(this.mAce.container.childNodes[6]);
              }

              this.load(to);
            }
          }
        break;
      }
    }
  }


  ngAfterViewInit() {
    this.mAce = ace.edit('editor');
    this.mAce.setTheme('ace/theme/crimson_editor');
    // TODO for later... each document gets a new session: this.mAce.createEditSesssion("session1", "Hello World!", Te);

    this.mAceSession = this.mAce.getSession()
    this.mAceSession.setMode('ace/mode/javascript');
    this.mAceSession.setUndoManager(new ace.UndoManager());
    this.mAceUndoManager = this.mAceSession.getUndoManager();

    // this.mAce.on('change', (event) => { });
    this.mAce.setOptions({
      fontFamily: 'Roboto Mono' // ,
      // enableBasicAutocompletion: true // we doesn't support all javaScript features... so we have to filter...
    });

    const wordList = [
      {'word': '$append(name, value);', 'meta': ''},
      {'word': '$delete(name);', 'meta': ''},
      {'word': '$load(name);', 'meta': ''},
      {'word': '$check(name);', 'meta': ''},
    ];

    const rhymeCompleter = {
        getCompletions: function(mAce, session, pos, prefix, callback) {
            if (prefix.length === 0) {
              callback(null, []);
              return;
            }

            callback(null, wordList.map(function(ea) {
              return {
                name: ea.word,
                value: ea.word,
                meta: ea.meta
              };
            }));
        }
    };

    const langTools = ace.require('ace/ext/language_tools');
    langTools.addCompleter(rhymeCompleter);

    this.mAce.setOptions({
      enableBasicAutocompletion: true,
      enableSnippets: true,
      enableLiveAutocompletion: true
    });

    /*this.mStatus = eStatus.EMPTY;

    if (!this.mAce.getReadOnly()) {
      this.mAce.setReadOnly(true);
      //this.mAce.container.style.pointerEvents="none";

      const cover = document.createElement("div");
      cover.innerText = "No file selected."
      this.mAce.container.appendChild(cover)
      cover.style.cssText = "position:absolute;\
      top:0;bottom:0;right:0;left:0;\
      background:#fbfbfb;\
      z-index:4;\
      text-align: center; vertical-align: middle;"
      cover.addEventListener("mousedown", function(e){e.stopPropagation()}, true);
    }*/
  }


  private load(path: string) {
    this.mApiStorage.load(this.src)
      .then((content) => {
        this.mAceSession.setValue(content, -1); // session.setValue() toggles UndoManager.reset()!
      })
      .catch(content => {
        // console.log(content); // this.snackBar.open('ERROR-4', null, {duration: 1000}) }
      });
  }

  public save(): void {
    this.mApiStorage.createOrSave(this.src, this.mAce.getValue())
      .then(result => {
        if (result) {
          this.mAceUndoManager.reset();
          // this.snackBar.open('Saved', null, {duration: 1000});
        } else {
          // this.snackBar.open('ERROR-3', null, {duration: 1000});
        }});
  }
}
