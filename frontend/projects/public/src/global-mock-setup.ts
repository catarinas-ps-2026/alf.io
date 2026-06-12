// This runs in the main Node process BEFORE jsdom is created
// It polyfills globalThis so that when jsdom inherits from it, window.matchMedia exists
export default function setup() {
  // @ts-ignore
  globalThis.matchMedia =
    globalThis.matchMedia ||
    function (query: string) {
      return {
        matches: false,
        media: query,
        onchange: null,
        addListener: function () {},
        removeListener: function () {},
        addEventListener: function () {},
        removeEventListener: function () {},
        dispatchEvent: function () {},
      };
    };
}
