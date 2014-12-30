//
//  MoMasterViewController.h
//  MoMobile
//
//  Created by Kwaku Zigah on 2014-10-22.
//  Copyright (c) 2014 ___FULLUSERNAME___. All rights reserved.
//

#import <UIKit/UIKit.h>

@class MoDetailViewController;

#import <CoreData/CoreData.h>

@interface MoMasterViewController : UITableViewController <NSFetchedResultsControllerDelegate>

@property (strong, nonatomic) MoDetailViewController *detailViewController;

@property (strong, nonatomic) NSFetchedResultsController *fetchedResultsController;
@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;

@end
