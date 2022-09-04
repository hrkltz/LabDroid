import { InjectionToken, TemplateRef } from '@angular/core';

export class ToastData {
  type: ToastType;
  text?: string;
  template?: TemplateRef<any>;
  templateContext?: {};
}

export type ToastType = 'warning' | 'info' | 'success' | 'error';

export enum ToastHorizontalPosition {
    'left',
    'center',
    'right'
}

export enum ToastVerticalPosition {
    'top',
    'bottom'
}

export interface ToastConfig {
    position?: {
        margin: number;
        horizontal: ToastHorizontalPosition;
        vertical: ToastVerticalPosition;
        
    };
    animation?: {
        fadeOut: number;
        fadeIn: number;
    };
}

export const defaultToastConfig: ToastConfig = {
    position: {
        margin: 10,
        horizontal: ToastHorizontalPosition.right,
        vertical: ToastVerticalPosition.top
    },
    animation: {
        fadeOut: 300,
        fadeIn: 300,
    },
};

export const TOAST_CONFIG_TOKEN = new InjectionToken('toast-config');