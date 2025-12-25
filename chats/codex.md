Removed the unused model.enums.Difficulty import from AppController (hygiene cleanup).

it.


I already removed the unused model.enums.Difficulty import from AppController.

Re: mapper directionality: today the only model→view conversion is difficulty in getGameLog(), and it’s handled inside ViewToModelMapper via toView(Difficulty). If more model→view mappings start showing up, we can either rename/split the mapper (e.g., ViewModelMapper with both directions, or separate ViewToModelMapper/ModelToViewMapper). For now, the surface is still clean and centralized.

