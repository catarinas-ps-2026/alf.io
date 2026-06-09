import { describe, it, expect } from 'vitest';
import { dispatchFeedback, type AlfioFeedbackEvent } from './dom-events.ts';

describe('dispatchFeedback', () => {
    // Técnicas: Particiones de equivalencia + Mocking con verificación
    // - PE1: Payload válido → evento despachado con detail correcto
    // - PE2: Verificar bubbles=true y composed=true
    // - PE3: Todos los tipos de feedback (neutral, success, warning, danger)
    function createMockLitElement(): EventTarget {
        return new EventTarget();
    }

    it('dispatches alfio-feedback event with correct detail', () => {
        const src = createMockLitElement();
        const payload: AlfioFeedbackEvent = {
            type: 'success',
            message: 'Operation completed',
        };

        let receivedEvent: CustomEvent<AlfioFeedbackEvent> | null = null;
        src.addEventListener('alfio-feedback', ((e: CustomEvent<AlfioFeedbackEvent>) => {
            receivedEvent = e;
        }) as EventListener);

        dispatchFeedback(payload, src);

        expect(receivedEvent).not.toBeNull();
        expect(receivedEvent!.detail).toEqual(payload);
    });

    it('dispatches event with bubbles=true', () => {
        const src = createMockLitElement();
        const payload: AlfioFeedbackEvent = { type: 'info', message: 'test' };

        let receivedEvent: CustomEvent<AlfioFeedbackEvent> | null = null;
        src.addEventListener('alfio-feedback', ((e: CustomEvent<AlfioFeedbackEvent>) => {
            receivedEvent = e;
        }) as EventListener);

        dispatchFeedback(payload, src);

        expect(receivedEvent!.bubbles).toBe(true);
    });

    it('dispatches event with composed=true', () => {
        const src = createMockLitElement();
        const payload: AlfioFeedbackEvent = { type: 'warning', message: 'test' };

        let receivedEvent: CustomEvent<AlfioFeedbackEvent> | null = null;
        src.addEventListener('alfio-feedback', ((e: CustomEvent<AlfioFeedbackEvent>) => {
            receivedEvent = e;
        }) as EventListener);

        dispatchFeedback(payload, src);

        expect(receivedEvent!.composed).toBe(true);
    });

    it('works with all feedback types', () => {
        const types: AlfioFeedbackEvent['type'][] = ['neutral', 'success', 'warning', 'danger'];

        for (const type of types) {
            const src = createMockLitElement();
            const payload: AlfioFeedbackEvent = { type, message: `test ${type}` };

            let receivedEvent: CustomEvent<AlfioFeedbackEvent> | null = null;
            src.addEventListener('alfio-feedback', ((e: CustomEvent<AlfioFeedbackEvent>) => {
                receivedEvent = e;
            }) as EventListener);

            dispatchFeedback(payload, src);

            expect(receivedEvent!.detail.type).toBe(type);
            expect(receivedEvent!.detail.message).toBe(`test ${type}`);
        }
    });
});
