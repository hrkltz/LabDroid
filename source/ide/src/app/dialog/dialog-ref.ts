import { Observable, Subject } from 'rxjs';

export class DialogRef {
  constructor() {}

  close(success: boolean, value: any = null) {
    this._afterClosed.next({
      'success': success, 
      'value': value
    })
  }

  closeSimple(success: boolean) {
    this._afterClosedSimple.next(success)
  }

  private readonly _afterClosed = new Subject<{ success: boolean, value: any }>()
  private readonly _afterClosedSimple = new Subject<boolean>()

  afterClosed: Observable<{ success: boolean, value: any }> = this._afterClosed.asObservable()
  afterClosedSimple: Observable<boolean> = this._afterClosedSimple.asObservable()
}
